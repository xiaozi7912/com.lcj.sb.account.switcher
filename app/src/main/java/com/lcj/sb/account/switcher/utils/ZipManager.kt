package com.lcj.sb.account.switcher.utils

import android.content.ContentResolver
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipManager {
    companion object {
        const val LOG_TAG = "ZipManager"

        @Throws(Exception::class)
        fun zip(files: ArrayList<String>, zipFile: String) {
            ZipOutputStream(FileOutputStream(zipFile)).use { outputStream ->
                for (file in files) {
                    FileInputStream(file).use { inputStream ->
                        val rootFolderName = zipFile.substring(zipFile.lastIndexOf("/") + 1, zipFile.lastIndexOf("."))
                        val entry = ZipEntry("${rootFolderName}/files/${file.substring(file.lastIndexOf("/") + 1)}")

                        outputStream.putNextEntry(entry)
                        outputStream.write(inputStream.readBytes())
                    }
                }
            }
        }

        @Throws(Exception::class)
        fun zip(resolver: ContentResolver, files: ArrayList<String>, zipFile: String) {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { outputStream ->
                for (file in files) {
                    resolver.openFileDescriptor(Uri.parse(file), "r").use { srcFile ->
                        FileInputStream(srcFile?.fileDescriptor).use { inputStream ->
                            val rootFolderName = zipFile.substring(zipFile.lastIndexOf("/") + 1, zipFile.lastIndexOf("."))
                            val replacedName = file.replace("%2F", "/")
                            val entry = ZipEntry("${rootFolderName}/files/${replacedName.substring(replacedName.lastIndexOf("/") + 1)}")

                            outputStream.putNextEntry(entry)
                            outputStream.putNextEntry(entry)
                            outputStream.write(inputStream.readBytes())
                        }
                    }
                }
            }
        }

        @Throws(Exception::class)
        fun unZip(zipFile: String, destPath: String) {
            ZipInputStream(FileInputStream(zipFile)).use { inputStream ->
                do {
                    val entry = inputStream.nextEntry?.apply {
                        val parents = name.split("/")
                        val fileName = name

                        File("$destPath/${parents[0]}").let {
                            if (!it.exists()) it.mkdir()
                        }

                        File("$destPath/${parents[0]}/${parents[1]}").let {
                            if (!it.exists()) it.mkdir()
                        }

                        FileOutputStream("$destPath/$fileName").use { outputStream ->
                            outputStream.write(inputStream.readBytes())
                        }
                        inputStream.closeEntry()
                    }
                } while (entry != null)
            }
        }

        @Throws(Exception::class)
        fun unZip(resolver: ContentResolver, rootDir: DocumentFile, zipFile: File) {
            ZipInputStream(FileInputStream(zipFile)).use { inputStream ->
                do {
                    val entry = inputStream.nextEntry?.apply {
                        val nodes = name.split("/")
                        val destDir = rootDir.findFile(nodes[0]) ?: rootDir.createDirectory(nodes[0])
                        val filesDir = destDir?.findFile(nodes[1]) ?: destDir?.createDirectory(nodes[1])
                        val file = filesDir?.findFile(nodes[2]) ?: filesDir?.createFile("", nodes[2])

                        file?.let {
                            resolver.openFileDescriptor(it.uri, "w")?.use { destFile ->
                                FileOutputStream(destFile.fileDescriptor).use { outputStream ->
                                    outputStream.write(inputStream.readBytes())
                                }
                            }
                        }
                        inputStream.closeEntry()
                    }
                } while (entry != null)
            }
        }
    }
}