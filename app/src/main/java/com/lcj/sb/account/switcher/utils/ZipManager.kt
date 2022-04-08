package com.lcj.sb.account.switcher.utils

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipManager {
    companion object {
        const val LOG_TAG = "ZipManager"

        fun zip(files: ArrayList<String>, zipFile: String) {
            var origin: BufferedInputStream
            val out = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))

            out.use {
                val data = ByteArray(10 * 1024)
                for (file in files) {
                    val fis = FileInputStream(file)
                    origin = BufferedInputStream(fis, 10 * 1024)

                    origin.use { origin ->
                        val rootFolderName = zipFile.substring(zipFile.lastIndexOf("/") + 1, zipFile.lastIndexOf("."))
                        val entry = ZipEntry("${rootFolderName}/files/${file.substring(file.lastIndexOf("/") + 1)}")
                        it.putNextEntry(entry)

                        var count: Int
                        do {
                            count = origin.read(data, 0, 10 * 1024)
                            if (count != -1) it.write(data, 0, count)
                        } while (count != -1)
                    }
                }
            }
        }

        @Throws(Exception::class)
        fun zip(resolver: ContentResolver, files: ArrayList<String>, zipFile: String) {
            var origin: BufferedInputStream
            val out = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))

            out.use {
                val data = ByteArray(10 * 1024)
                var fis: InputStream? = null

                try {
                    for (file in files) {
                        fis = resolver.openInputStream(Uri.parse(file))
                        origin = BufferedInputStream(fis, 10 * 1024)

                        origin.use { origin ->
                            val rootFolderName = zipFile.substring(zipFile.lastIndexOf("/") + 1, zipFile.lastIndexOf("."))
                            val replacedName = file.replace("%2F", "/")
                            val entry = ZipEntry("${rootFolderName}/files/${replacedName.substring(replacedName.lastIndexOf("/") + 1)}")
                            it.putNextEntry(entry)

                            var count: Int
                            do {
                                count = origin.read(data, 0, 10 * 1024)
                                if (count != -1) it.write(data, 0, count)
                            } while (count != -1)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                } finally {
                    fis?.close()
                }
            }
        }

        fun unZip(zipFile: String, destPath: String) {
            val zis = ZipInputStream(BufferedInputStream(FileInputStream(zipFile)))
            try {
                val buffer = ByteArray(10 * 1024)
                do {
                    val ze = zis.nextEntry?.apply {
                        val parents = name.split("/")
                        val fileName = name

                        File("$destPath/${parents[0]}").let {
                            if (!it.exists()) it.mkdir()
                        }

                        File("$destPath/${parents[0]}/${parents[1]}").let {
                            if (!it.exists()) it.mkdir()
                        }

                        FileOutputStream("$destPath/$fileName").let {
                            do {
                                val count = zis.read(buffer)
                                if (count != -1) it.write(buffer, 0, count)
                            } while (count != -1)
                            it.close()
                        }
                        zis.closeEntry()
                    }
                } while (ze != null)
                zis.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun unZip(resolver: ContentResolver, rootDir: DocumentFile, zipFile: File) {
            try {
                val zis = ZipInputStream(BufferedInputStream(FileInputStream(zipFile)))
                val buffer = ByteArray(10 * 1024)

                do {
                    val ze = zis.nextEntry?.apply {
                        val nodes = name.split("/")
                        val destDir = rootDir.findFile(nodes[0]) ?: rootDir.createDirectory(nodes[0])
                        val filesDir = destDir?.findFile(nodes[1]) ?: destDir?.createDirectory(nodes[1])
                        val file = filesDir?.findFile(nodes[2]) ?: filesDir?.createFile("", nodes[2])
                        Log.d(LOG_TAG, "destDir?.uri : ${destDir?.uri}")
                        Log.d(LOG_TAG, "filesDir?.uri : ${filesDir?.uri}")
                        Log.d(LOG_TAG, "file?.uri : ${file?.uri}")
                        file?.let {
                            BufferedOutputStream(resolver.openOutputStream(it.uri)).let { stream ->
                                do {
                                    val count = zis.read(buffer)
                                    if (count != -1) stream.write(buffer, 0, count)
                                } while (count != -1)
                                stream.close()
                            }
                            zis.closeEntry()
                        }
                    }
                } while (ze != null)
                zis.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}