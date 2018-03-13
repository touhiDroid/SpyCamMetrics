package com.touhiDroid.spycammetrics

import java.io.*
import java.util.*

class Logger {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runLogger()
        }

        private fun runLogger() {
            val task = object : TimerTask() {
                override fun run() {
                    dumpLog()
                }

            }
            Timer().scheduleAtFixedRate(task, Date(), 5 * 60 * 1000)
        }

        private fun dumpLog() {
            val process = Runtime.getRuntime()
                    .exec("adb shell dumpsys batterystats --checkin")// >> ${System.currentTimeMillis()}.csv")

            val writer = BufferedWriter(FileWriter(File("results", "adbBatStats_Nexus6_${System.currentTimeMillis()}.csv")))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line = reader.readLine()
            while (line != null) {
                writer.append(line + "\n")
                line = reader.readLine()
            }
            writer.flush()
            writer.close()
            reader.close()
            process.waitFor()
        }
    }
}

/**Logging using ProcessBuilder(recommended but untested for now) instead of Runtime*/
/*public class TestMain {
    public static void main(String a[]) throws InterruptedException {

        List<String> commands = new ArrayList<String>();
        commands.add("adb");
        commands.add("shell");
        commands.add("dumpsys");
        commands.add("batterystats");
        commands.add(" --checkin");
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);
        try {

            Process prs = pb.start();
            Thread inThread = new Thread(new In(prs.getInputStream()));
            inThread.start();
            Thread.sleep(2000);
            OutputStream writeTo = prs.getOutputStream();
            writeTo.write("oops\n".getBytes());
            writeTo.flush();
            writeTo.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class In implements Runnable {
    private InputStream is;

    public In(InputStream is) {
        this.is = is;
    }

    @Override
    public void run() {
        byte[] b = new byte[1024];
        int size = 0;
        try {
            while ((size = is.read(b)) != -1) {
                System.err.println(new String(b));
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}*/