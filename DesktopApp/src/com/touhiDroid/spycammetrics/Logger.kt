package com.touhiDroid.spycammetrics

import java.io.*

class Logger{
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val process = Runtime.getRuntime()
                    .exec("adb shell dumpsys batterystats --checkin")// >> ${System.currentTimeMillis()}.csv")
            // process.waitFor()

            val writer = BufferedWriter(FileWriter(File("/results/${System.currentTimeMillis()}.csv")))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line = reader.readLine()
            while (line != null) {
                writer.append(line + "\n")
                line = reader.readLine()
            }
            writer.flush()
            writer.close()
            reader.close()
        }
    }
}