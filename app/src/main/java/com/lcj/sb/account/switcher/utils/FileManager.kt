package com.lcj.sb.account.switcher.utils

import android.content.Context
import android.content.pm.PackageManager
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.FolderSync
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.util.regex.Pattern


class FileManager {
    interface BackupCallback {
        fun onProcess(current: Int, total: Int)
        fun onCompleted()
        fun onError()
    }

    interface LoadCallback {
        fun onCompleted()
        fun onError()
    }

    companion object {
        private const val LOG_TAG = "FileManager"
        private const val BUFFER_SIZE = 1024

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
            if (file.isDirectory && file.exists()) {
                return true
            }
            return false
        }

        fun syncBackupFolder(context: Context, lang: Account.Language, callback: () -> Unit) {
            Observable.just(File(Configs.PATH_APP_DATA))
                    .flatMap { Observable.fromArray(*it.listFiles()) }
                    .filter {
                        Pattern.compile(when (lang) {
                            Account.Language.JP -> String.format("%s\\.\\w+", Configs.PREFIX_NAME_SB_JP)
                            Account.Language.TW -> String.format("%s\\.\\w+", Configs.PREFIX_NAME_SB_TW)
                        }).matcher(it.name).matches()
                    }.sorted()
                    .doOnNext {
                        val db = BaseDatabase.getInstance(context)
                        val currentTime = System.currentTimeMillis()
                        val existsAccount = db.accountDAO().account(it.absolutePath)

                        if (existsAccount == null) {
                            db.accountDAO().insert(Account(
                                    alias = it.name,
                                    folder = it.absolutePath,
                                    lang = lang.ordinal,
                                    createTime = currentTime,
                                    updateTime = currentTime))
                        } else {
                            db.accountDAO().update(existsAccount.apply {
                                hidden = false
                            })
                        }
                    }
                    .doOnComplete {
                        val db = BaseDatabase.getInstance(context)
                        val currentTime = System.currentTimeMillis()

                        db.folderSyncDAO().folderSync(FolderSync.Type.LOCAL.ordinal, lang.ordinal)
                                .subscribe({ entity ->
                                    entity!!.updateTime = currentTime
                                    db.folderSyncDAO().update(entity!!)
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
            val destDir = File(destPath)
            val destFilesDir = File(String.format("%s/files", destPath))
            val resFileList = resDir.listFiles()

            destDir.mkdir()
            destFilesDir.mkdir()
            destFilesDir.setLastModified(System.currentTimeMillis())

            if (resFileList != null) {
                resFileList.filter { it.name == "files" }.forEach { file ->
                    val fileList = file.listFiles(FileFilter { it.isFile })
                    var current = 0
                    val totalSize = fileList.size

                    fileList.forEach {
                        copyFile(it.absolutePath, String.format("%s/%s", destFilesDir.absolutePath, it.name))
                        current++
                        callback.onProcess(current, totalSize)
                    }
                    callback.onCompleted()
                }
            } else {
                callback.onError()
            }
        }

        fun loadFolder(resPath: String, destPath: String, callback: LoadCallback) {
            Thread {
                val dstDir = File(destPath)
                val resFilesDir = File(resPath, "files")
                val dstFilesDir = File(destPath, "files")

                if (!dstDir.exists()) {
                    dstDir.mkdir()
                    dstFilesDir.mkdir()
                }

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
            }.start()
        }

        fun getFolderList(lang: Account.Language, callback: (dataList: ArrayList<File>) -> Unit) {
            val result = arrayListOf<File>()
            val d = Observable.just(File(Configs.PATH_APP_DATA))
                    .flatMap { Observable.fromArray(*it.listFiles()) }
                    .filter {
                        Pattern.compile(when (lang) {
                            Account.Language.JP -> String.format("%s\\.\\w+", Configs.PREFIX_NAME_SB_JP)
                            Account.Language.TW -> String.format("%s\\.\\w+", Configs.PREFIX_NAME_SB_TW)
                        }).matcher(it.name).matches()
                    }.sorted()
                    .subscribeOn(Schedulers.io())
                    .subscribe({ file ->
                        result.add(file)
                    }, { err -> err.printStackTrace() }, { callback(result) })
        }

        private fun copyFile(resFile: String, destFile: String) {
            var bis: BufferedInputStream? = null
            var bos: BufferedOutputStream? = null

            try {
                bis = BufferedInputStream(FileInputStream(File(resFile)))
                bos = BufferedOutputStream(FileOutputStream(File(destFile), false))

                val buff = ByteArray(BUFFER_SIZE)
                var readSize: Int

                do {
                    readSize = bis.read(buff)
                    if (readSize > 0) bos.write(buff, 0, readSize)
                } while (readSize > 0)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    bis!!.close()
                    bos!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}