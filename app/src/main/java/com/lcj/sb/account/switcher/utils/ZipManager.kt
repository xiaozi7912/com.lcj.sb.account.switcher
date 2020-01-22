package com.lcj.sb.account.switcher.utils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
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
                        val entry = ZipEntry("files/${file.substring(file.lastIndexOf("/") + 1)}")
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

        fun unZip() {}
    }
}