package com.touhidroid.spycammetrics

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.TrafficStats
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // tvNetStat.text
        // Get running processes
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
            val s = "$uid:${pm.getApplicationLabel(app)} ::-> Send : $send, Received : $received"
            tvNetStat.text = "${tvNetStat.text}\n$s"
            Log.v(TAG, s)
        }
    }
}
