package com.lcj.sb.account.switcher.utils

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.FolderSync
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.util.regex.Pattern

class FileManager {
    interface BackupCallback {
        fun onProcess(current: Int, total: Int)
        fun onCompleted(folderPath: String)
        fun onError(message: String)
    }

    interface LoadCallback {
        fun onCompleted()
        fun onError()
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

        fun backupFolder(resPath: String, destPath: String, callback: BackupCallback) {
            val resDir = File(resPath)
            val destFilesDir = File(destPath, "files")
            val resFileList = resDir.listFiles()

            if (!destFilesDir.exists()) destFilesDir.mkdirs()
            destFilesDir.setLastModified(System.currentTimeMillis())

            resFileList?.filter { it.name == "files" }?.forEach { dir ->
                val fileList = dir?.listFiles()?.filter { item -> item.isFile }
                var current = 0
                val totalSize = fileList?.size ?: 0

                fileList?.forEach {
                    copyFile(it.absolutePath, String.format("%s/%s", destFilesDir.absolutePath, it.name))
                    current++
                    callback.onProcess(current, totalSize)
                }
                callback.onCompleted(destPath)
            } ?: run { callback.onError("沒有資料。") }
        }

        fun backupFolder(resolver: ContentResolver, rootDir: DocumentFile, resDirName: String, destDirName: String, callback: BackupCallback) {
            val resDir = rootDir.findFile(resDirName)
            val destDir = rootDir.findFile(destDirName) ?: rootDir.createDirectory(destDirName)
            val destFilesDir = destDir?.findFile("files") ?: destDir?.createDirectory("files")

            resDir?.let {
                val resFileList = it.listFiles()

                if (resFileList.isEmpty()) {
                    callback.onError("遊戲資料夾內沒資料。")
                    return
                }

                resFileList.filter { file -> file.name == "files" }.forEach { dir ->
                    val fileList = dir.listFiles().filter { item -> item.isFile }
                    var current = 0
                    val totalSize = fileList.size

                    fileList.forEach { file ->
                        val destFile = destFilesDir?.findFile(file.name!!) ?: destFilesDir?.createFile(file.type ?: "", file.name ?: "")

                        try {
                            copyFile(resolver, file.uri, destFile?.uri!!)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        current++
                        callback.onProcess(current, totalSize)
                    }
                    callback.onCompleted(destDir?.uri.toString())
                }
            } ?: run { callback.onError("沒有資料。") }
        }

        fun loadFolder(resPath: String, destPath: String, callback: LoadCallback) {
            val d = CompletableFromAction.fromAction {
                val resFilesDir = File(resPath, "files")
                val dstFilesDir = File(destPath, "files")

                if (!resFilesDir.exists()) resFilesDir.mkdirs()
                if (!dstFilesDir.exists()) dstFilesDir.mkdirs()

                try {
                    resFilesDir.listFiles()?.let { list ->
                        if (list.isEmpty()) {
                            callback.onError()
                        } else {
                            list.forEach { file ->
                                val dstFilePath = String.format("%s/%s", dstFilesDir.absolutePath, file.name)
                                copyFile(file.absolutePath, dstFilePath)
                            }
                            callback.onCompleted()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onError()
                }
            }
                .subscribeOn(Schedulers.io())
                .subscribe { }
        }

        fun loadFolder(resolver: ContentResolver, rootDir: DocumentFile, srcDirName: String, destDirName: String, callback: LoadCallback) {
            val d = CompletableFromAction.fromAction {
                val srcDir = rootDir.findFile(srcDirName) ?: rootDir.createDirectory(srcDirName)
                val srcFilesDir = srcDir?.findFile("files") ?: srcDir?.createDirectory("files")
                val destDir = rootDir.findFile(destDirName) ?: rootDir.createDirectory(destDirName)
                val destFilesDir = destDir?.findFile("files") ?: destDir?.createDirectory("files")

                try {
                    destFilesDir?.listFiles()?.let { destFileList ->
                        destFileList.filter { item -> item.isFile }.forEach { file -> file.delete() }
                    }
                    srcFilesDir?.listFiles()?.let { srcFileList ->
                        srcFileList.forEach { srcFile ->
                            val fileName = srcFile.name ?: ""
                            val destFile = destFilesDir?.findFile(fileName) ?: destFilesDir?.createFile("", fileName)
                            copyFile(resolver, srcFile.uri, destFile?.uri!!)
                        }
                        callback.onCompleted()
                    } ?: run { callback.onError() }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onError()
                }
            }
                .subscribeOn(Schedulers.io())
                .subscribe { }
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