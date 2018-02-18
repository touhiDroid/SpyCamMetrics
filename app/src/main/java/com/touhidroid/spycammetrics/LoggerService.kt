package com.touhidroid.spycammetrics

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.Environment
import android.os.Handler
import android.util.Log
import java.io.*

/**
 * Created by touhid on 2/18/2018.
 * bismillah :)
 */
class LoggerService : IntentService("") {
    private val TAG = LoggerService::class.java.simpleName

    private val sysDir = File(Environment.getExternalStorageDirectory(), "SpyCamMetrics/sys")
    private val netsDir = File(Environment.getExternalStorageDirectory(), "SpyCamMetrics/nets")

    private val handler = Handler()
    private val lastSentNetMap = HashMap<Int, Long>()
    private val lastRecvNetMap = HashMap<Int, Long>()

    private val logger = Runnable { dumpLog() }


    private fun dumpLog() {
        if (!sysDir.exists())
            sysDir.mkdirs()
        if (!netsDir.exists())
            netsDir.mkdirs()
        netLogger()
        sysLogger()
        handler.postDelayed(logger, 5 * 60 * 1000)
    }

    private fun sysLogger() {
        try {
            // Executes the command.
            val process = Runtime.getRuntime().exec("adb shell dumpsys batterystats --checkin")
            process.waitFor()

            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            // val strBuffer = StringBuffer()
            val writer = BufferedWriter(FileWriter(File(sysDir, "${System.currentTimeMillis()}.csv")))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line = reader.readLine()
            while (line != null) {
                writer.append(line + "\n")
                line = reader.readLine()
            }
            writer.flush()
            writer.close()
            reader.close()
            // Waits for the command to finish.
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun netLogger() {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps = manager.runningAppProcesses
        Log.d(TAG, "Number of running apps: ${runningApps.size}")

        val bfos = BufferedOutputStream(
                FileOutputStream(File(netsDir, "${System.currentTimeMillis()}.csv")))

        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
        for (app in apps) {

            // Get UID of the selected process
            val uid = app.uid// (getListAdapter().getItem(position) as RunningAppProcessInfo).uid

            // Get traffic data
            val received = TrafficStats.getTotalRxBytes()
            val sent = TrafficStats.getTotalTxBytes()

            val dr = received - (lastRecvNetMap[uid] ?: 0)
            val ds = sent - (lastSentNetMap[uid] ?: 0)
            try {
                bfos.write("$uid,${pm.getApplicationLabel(app)},$dr,$ds\n".toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            lastSentNetMap.put(uid, sent)
            lastRecvNetMap.put(uid, received)

            val s = "$uid:${pm.getApplicationLabel(app)} ::-> Send : $sent, Received : $received"
            // tvNetStat.text = "${tvNetStat.text}\n$s"
            Log.v(TAG, s)
        }
        try {
            bfos.flush()
            bfos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        dumpLog()
    }

    override fun onDestroy() {
        lastRecvNetMap.clear()
        lastSentNetMap.clear()
        handler.removeCallbacks(logger)
        super.onDestroy()
    }

}