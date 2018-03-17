package com.touhidroid.spycam.dataparser

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.collections.ArrayList


class DataParser {
    companion object {
        private val PACKAGE_NAMES = arrayOf("com.bangladesharmy", "com.imo.android.imoim", "com.whatsapp",
                "com.facebook.orca", "com.google.android.talk")

        /**
         *  Parsing-Algorithm
         *
        // for every file in the input-directory
        //      for every package in the package-array
        //          app-id = package-pos in $PACKAGE_NAMES
        //          find first occurrence of the package-name
        //          get the 5th column value as $uid if the 4th column is "uid"
        //          for every row with the $uid
        //              if(bt) -> plot in battery_info file
        //              else if(nt) -> plot int net_info with
        //              else if(fg) -> launch_count_info
        //              else if(...) -> ...*/
        @JvmStatic
        fun main(args: Array<String>) {
            val files = File("in").listFiles()
            for (f in files) {
                if (!f.name.endsWith(".csv")) continue
                for (appPos in 0 until PACKAGE_NAMES.size) {
                    val app = PACKAGE_NAMES[appPos]

                    val rowList = getUidRows(f, app)
                    if (rowList.size < 1) {
                        println("No compatible record found for $app in file=${f.absolutePath}")
                        continue
                    }
                    val fileName = f.name
                    val timeStamp = fileName.substring(fileName.lastIndexOf("_") + 1,
                            fileName.lastIndexOf("."))
                    for (row in rowList) {
                        val tokens = tokenizeCSVRow(row)
                        saveInfo(when (tokens[3]) {
                            "bt" -> "battery_info.csv"
                            "nt" -> "network_info.csv"
                            "gn" -> "network_info.csv"
                            "wl" -> "wake_lock_info.csv"    // <- Most-Significant to catch SpyCam
                            "fg" -> "foreground_info.csv"
                            "pr" -> "process_info.csv"
                            "st" -> "process_info.csv"
                            else -> null
                        }, timeStamp, app, row)
                    }
                }
            }
        }

        private fun saveInfo(targetFileName: String?, timeStamp: String, appPkg: String, dataRow: String) {
            if (targetFileName == null || targetFileName.isEmpty())
                return
            try {
                val path = Paths.get(targetFileName)
                Files.write(path,
                        ("$timeStamp,$appPkg,$dataRow\n").toByteArray(),
                        if (Files.exists(path)) StandardOpenOption.APPEND else StandardOpenOption.CREATE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun tokenizeCSVRow(row: String): ArrayList<String> {
            val tokenizer = StringTokenizer(row, ",")
            val tokens = ArrayList<String>()
            while (tokenizer.hasMoreTokens())
                tokens.add(tokenizer.nextToken())
            return tokens
        }

        private fun getUidRows(f: File, appPkg: String): ArrayList<String> {
            val rowList = ArrayList<String>()
            Files.lines(Paths.get(f.absolutePath)).use { strStream ->
                try {
                    var uid = 0.toLong()
                    for (line in strStream)
                        if (uid < 1) {
                            if (line.contains(appPkg)) {
                                val tokens = tokenizeCSVRow(line)
                                if (tokens.size > 3 && tokens[3] == "uid")
                                    uid = tokens[4].toLong()
                            }
                        } else if (line.contains(uid.toString()))
                            rowList.add(line)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    strStream.close()
                }
            }
            return rowList
        }
    }
}

/*val fis = BufferedReader(FileReader(f))
val rowList = fis.readLines()
for (row in rowList) {
    //
}*/