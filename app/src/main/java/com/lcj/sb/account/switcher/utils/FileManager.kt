package com.lcj.sb.account.switcher.utils

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.lcj.sb.account.switcher.BuildConfig
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.FolderSync
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.regex.Pattern

class FileManager {
    interface BackupCallback {
        fun onProcess(current: Int, total: Int)
        fun onCompleted(folderPath: String)
        fun onError(message: String)
    }

    interface LoadCallback {
        fun onCompleted()
        fun onError(message: String = "")
    }

    companion object {
        private const val LOG_TAG = "FileManager"

        fun isPackageInstalled(packageName: String, context: Context): Boolean {
            return try {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        fun isFolderExists(path: String): Boolean {
            val file = File(path)
            if (file.isDirectory && file.exists()) return true
            return false
        }

        fun isFolderExists(rootDirectory: DocumentFile, dirName: String): Boolean {
            var exists = false
            for (file in rootDirectory.listFiles()) {
                if (file.name == dirName && file.isDirectory) {
                    exists = true
                    break
                }
            }
            return exists
        }

        fun syncBackupFolder(context: Context, lang: Account.Language, callback: () -> Unit) {
            Observable.just(File(Configs.PATH_APP_DATA))
                .flatMap { Observable.fromArray(*it.listFiles()) }
                .filter {
                    Pattern.compile(
                        when (lang) {
                            Account.Language.JP -> String.format("%s\\.\\w+", Configs.PREFIX_NAME_SB_JP)
                            Account.Language.TW -> String.format("%s\\.\\w+", Configs.PREFIX_NAME_SB_TW)
                        }
                    ).matcher(it.name).matches()
                }.sorted()
                .doOnNext {
                    val db = BaseDatabase.getInstance(context)
                    val currentTime = System.currentTimeMillis()
                    val existsAccount = db.accountDAO().account(it.absolutePath)

                    if (existsAccount == null) {
                        db.accountDAO().insert(
                            Account(
                                alias = it.name, folder = it.absolutePath, lang = lang.ordinal, createTime = currentTime, updateTime = currentTime
                            )
                        )
                    } else {
                        db.accountDAO().update(existsAccount.apply {
                            hidden = false
                        })
                    }
                }
                .doOnComplete {
                    val db = BaseDatabase.getInstance(context)
                    val currentTime = System.currentTimeMillis()

                    val d = db.folderSyncDAO().folderSync(FolderSync.Type.LOCAL.ordinal, lang.ordinal)
                        .subscribe({ entity ->
                            entity!!.updateTime = currentTime
                            db.folderSyncDAO().update(entity)
                        }, { err ->
                            err.printStackTrace()
                            db.folderSyncDAO().insert(FolderSync(FolderSync.Type.LOCAL.ordinal, lang.ordinal, currentTime))
                        })
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete { callback() }
                .subscribe()
        }

        fun backupFolder(context: Context, gameFolderName: String, destFolderName: String, callback: BackupCallback) {
            val destPath = String.format("%s/%s/%s", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), BuildConfig.APPLICATION_ID, destFolderName)
            val destFilesDir = File(destPath, "files")

            if (!destFilesDir.mkdirs()) Log.e(LOG_TAG, "create dir error.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.persistedUriPermissions.find { it.uri.toString().contains(gameFolderName) }?.let { uriPermission ->
                    DocumentFile.fromTreeUri(context, uriPermission.uri)?.let { gameDir ->
                        gameDir.findFile("files")?.let { filesDir ->
                            if (!filesDir.exists()) {
                                callback.onError("遊戲資料夾內沒資料。")
                                return
                            }

                            filesDir.listFiles().filter { file -> file.isFile }.let { fileList ->
                                var current = 0
                                val totalSize = fileList.size

                                for (documentFile in fileList) {
                                    try {
                                        val destFile = File(destFilesDir, documentFile.name ?: "")
                                        copyFile(context.contentResolver, documentFile.uri, destFile.toUri())
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    current++
                                    callback.onProcess(current, totalSize)
                                }
                                callback.onCompleted(destPath)
                            }
                        }
                    }
                }
            } else {
                val gameDir = File(gameFolderName)
                gameDir.listFiles()?.filter { it.name == "files" }?.forEach { dir ->
                    val fileList = dir?.listFiles()?.filter { item -> item.isFile }
                    var current = 0
                    val totalSize = fileList?.size ?: 0

                    fileList?.forEach {
                        copyFile(it.absolutePath, String.format("%s/%s", destFilesDir.absolutePath, it.name))
                        current++
                        callback.onProcess(current, totalSize)
                    }
                    callback.onCompleted(destFolderName)
                } ?: run { callback.onError("沒有資料。") }
            }
        }

        fun loadFolder(context: Context, gameFolderName: String, accountFolderPath: String, callback: LoadCallback) {
            val gameFolderPath = String.format("%s/%s/%s", Environment.getExternalStorageDirectory(), "Android/data", gameFolderName)
            val gameFilesFolder = File(gameFolderPath, "files")
            val accFilesFolder = File(accountFolderPath, "files")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.persistedUriPermissions.find { it.uri.toString().contains(gameFolderName) }?.let { uriPermission ->
                    DocumentFile.fromTreeUri(context, uriPermission.uri)?.let { gameDir ->
                        gameDir.findFile("files")?.listFiles()?.let { list ->
                            for (documentFile in list.filter { file -> file.isFile }) {
                                documentFile.delete()
                            }
                        }

                        accFilesFolder.listFiles()?.let { list ->
                            if (list.isEmpty()) {
                                callback.onError("Account files folder is Empty.")
                            } else {
                                for (file in list) {
                                    gameDir.findFile("files")?.let { filesDir ->
                                        val destFile = filesDir.findFile(file.name) ?: filesDir.createFile("", file.name)
                                        copyFile(context.contentResolver, file.toUri(), destFile?.uri!!)
                                    }
                                }
                                callback.onCompleted()
                            }
                        }
                    } ?: run { callback.onError() }
                }
            } else {
                try {
                    accFilesFolder.listFiles()?.let { list ->
                        if (list.isEmpty()) {
                            callback.onError()
                        } else {
                            for (file in list) {
                                val destFile = String.format("%s/%s", gameFilesFolder.absolutePath, file.name)
                                copyFile(file.absolutePath, destFile)
                            }
                            callback.onCompleted()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onError()
                }
            }
        }

        @Throws(Exception::class)
        private fun copyFile(srcFilePath: String, destFilePath: String) {
            FileInputStream(srcFilePath).use { inputStream ->
                FileOutputStream(destFilePath).use { outputStream ->
                    outputStream.write(inputStream.readBytes())
                }
            }
        }

        @Throws(Exception::class)
        private fun copyFile(resolver: ContentResolver, srcUri: Uri, destUri: Uri) {
            resolver.openFileDescriptor(srcUri, "r")?.use { srcFile ->
                FileInputStream(srcFile.fileDescriptor).use { inputStream ->
                    resolver.openFileDescriptor(destUri, "w")?.use { destFile ->
                        FileOutputStream(destFile.fileDescriptor).use { outputStream ->
                            outputStream.write(inputStream.readBytes())
                        }
                    }
                }
            }
        }
    }
}