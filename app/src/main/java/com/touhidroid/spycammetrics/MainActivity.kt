package com.touhidroid.spycammetrics

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private val PERM_CODE_WRITE_STORAGE = 128


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (PermissionManager.requestSinglePermission(this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, 101,
                        "Please grant permission to save the log-files."))
            startLogging()
        // startService(Intent(applicationContext, LoggerService::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERM_CODE_WRITE_STORAGE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startLogging()
        // startService(Intent(applicationContext, LoggerService::class.java))
            else -> {
            }
        }
    }

    private fun startLogging() {
        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, LoggerService::class.java)
        val alarmIntent = PendingIntent.getService(applicationContext, 0, intent, 0)

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100,
                1000 * 60 * 1, alarmIntent)
    }

}
