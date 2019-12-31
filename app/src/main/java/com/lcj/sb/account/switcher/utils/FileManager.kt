package com.lcj.sb.account.switcher.utils

import android.content.pm.PackageManager
import android.util.Log
import java.io.*


class FileManager {
    companion object {
        const val LOG_TAG = "FileManager"
        const val BUFFER_SIZE = 512

        fun isPackageInstalled(packageName: String, pm: PackageManager): Boolean {
            return try {
                pm.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        fun isFolderExists(path: String): Boolean {
            Log.i(LOG_TAG, "isFolderExists")
            val file = File(path)
            if (file.isDirectory && file.exists()) {
                return true
            }
            return false
        }

        fun backupFolder(resPath: String, destPath: String, callback: (Int, Int, Boolean) -> Unit) {
            Log.i(LOG_TAG, "backupFolder")
            Log.v(LOG_TAG, "backupFolder resPath : $resPath")
            Log.v(LOG_TAG, "backupFolder destPath : $destPath")
            val resDir = File(resPath)
            val destDir = File(destPath)
            val destFilesDir = File(String.format("%s/files", destPath))

            destDir.mkdir()
            destFilesDir.mkdir()
            destFilesDir.setLastModified(System.currentTimeMillis())

            resDir.listFiles(FileFilter { it.name == "files" })
                    .forEach { file ->
                        Log.v(LOG_TAG, "backupFolder absolutePath : ${file.absolutePath}")
                        val fileList = file.listFiles(FileFilter { it.isFile })
                        var current = 0
                        val totalSize = fileList.size
                        fileList.forEach {
                            Log.v(LOG_TAG, "backupFolder absolutePath : ${it.absolutePath}")
                            copyFile(it.absolutePath, String.format("%s/%s", destFilesDir.absolutePath, it.name))
                            current++
                            callback(current, totalSize, (current == totalSize))
                        }
                    }
        }

        private fun copyFile(resFile: String, destFile: String) {
            Log.i(LOG_TAG, "copyFile")
            Log.v(LOG_TAG, "copyFile resFile : $resFile")
            Log.v(LOG_TAG, "copyFile destFile : $destFile")
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