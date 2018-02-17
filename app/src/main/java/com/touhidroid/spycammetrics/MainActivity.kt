package com.touhidroid.spycammetrics

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.net.TrafficStats
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    val handler = Handler()
    val lastSentNetMap = HashMap<Int, Long>()
    val lastRecvNetMap = HashMap<Int, Long>()

    val logger = Runnable { dumpLog() }

    private fun dumpLog() {

        netLogger()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionManager.requestSinglePermission(this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, 101,
                "Please grant permission to export the DB.")

        // tvNetStat.text
        // Get running processes
        netLogger()
    }

    private fun sysLogger() {
        try {
            // Executes the command.
            val process = Runtime.getRuntime().exec("adb shell dumpsys batterystats --checkin")

            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            var read = 0
            var buffer = charArrayOf()
            val output = StringBuffer()
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();

            return output.toString();
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun netLogger() {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps = manager.runningAppProcesses
        Log.d(TAG, "Number of running apps: " + runningApps.size)

        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
        for (app in apps) {

            // Get UID of the selected process
            val uid = app.uid// (getListAdapter().getItem(position) as RunningAppProcessInfo).uid

            // Get traffic data
            val received = TrafficStats.getTotalRxBytes()
            val send = TrafficStats.getTotalTxBytes()
            lastSentNetMap.put(uid, send)
            lastRecvNetMap.put(uid, received)

            val s = "$uid:${pm.getApplicationLabel(app)} ::-> Send : $send, Received : $received"
            tvNetStat.text = "${tvNetStat.text}\n$s"
            Log.v(TAG, s)
        }
    }
}
