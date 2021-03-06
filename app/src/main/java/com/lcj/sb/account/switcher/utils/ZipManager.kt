package com.lcj.sb.account.switcher.utils

import android.util.Log
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipManager {
    companion object {
        fun zip(files: ArrayList<String>, zipFile: String) {
            var origin: BufferedInputStream
            val out = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))

            out.use { out ->
                val data = ByteArray(10 * 1024)
                for (file in files) {
                    val fis = FileInputStream(file)
                    origin = BufferedInputStream(fis, 10 * 1024)

                    origin.use { origin ->
                        val rootFolderName = zipFile.substring(zipFile.lastIndexOf("/") + 1, zipFile.lastIndexOf("."))
                        val entry = ZipEntry("${rootFolderName}/files/${file.substring(file.lastIndexOf("/") + 1)}")
                        out.putNextEntry(entry)

                        var count: Int
                        do {
                            count = origin.read(data, 0, 10 * 1024)
                            if (count != -1) out.write(data, 0, count)
                        } while (count != -1)
                    }
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
    }
}