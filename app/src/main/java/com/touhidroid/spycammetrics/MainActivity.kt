package com.touhidroid.spycammetrics

import android.Manifest
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
            startService(Intent(applicationContext, LoggerService::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERM_CODE_WRITE_STORAGE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startService(Intent(applicationContext, LoggerService::class.java))
            else -> {
            }
        }
    }
}
