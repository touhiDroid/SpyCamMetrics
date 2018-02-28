package com.touhidroid.spycammetrics

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.util.Log
import java.io.*

/**
 * Created by touhid on 2/18/2018.
 * bismillah :)
 */
class LoggerService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        Log.d(TAG, "onHandleIntent : intent=$p0")
        return null
        /*object : IBinder{
            override fun getInterfaceDescriptor(): String {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun isBinderAlive(): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun linkToDeath(p0: IBinder.DeathRecipient?, p1: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun queryLocalInterface(p0: String?): IInterface {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun transact(p0: Int, p1: Parcel?, p2: Parcel?, p3: Int): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun dumpAsync(p0: FileDescriptor?, p1: Array<out String>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun dump(p0: FileDescriptor?, p1: Array<out String>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun unlinkToDeath(p0: IBinder.DeathRecipient?, p1: Int): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun pingBinder(): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }*/
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        dumpLog()
    }

    private val TAG = LoggerService::class.java.simpleName

    private val sysDir = File(Environment.getExternalStorageDirectory(), "SpyCamMetrics/sys")
    private val netsDir = File(Environment.getExternalStorageDirectory(), "SpyCamMetrics/nets")

    // private val handler = Handler()
    // private val lastSentNetMap = HashMap<Int, Long>()
    // private val lastRecvNetMap = HashMap<Int, Long>()

    private val logger = Runnable { dumpLog() }


    private fun dumpLog() {
        if (!sysDir.exists())
            sysDir.mkdirs()
        if (!netsDir.exists())
            netsDir.mkdirs()
        netLogger()
        sysLogger()
        Handler().postDelayed({ stopSelf() }, 2 * 1000)
        // handler.postDelayed(logger, 5 * 60 * 1000)
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
            val received = TrafficStats.getUidRxBytes(uid)
            val sent = TrafficStats.getUidTxBytes(uid)

            val dr = received // - (lastRecvNetMap[uid] ?: 0)
            val ds = sent // - (lastSentNetMap[uid] ?: 0)
            try {
                bfos.write("$uid,${pm.getApplicationLabel(app)},$dr,$ds\n".toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            // lastSentNetMap.put(uid, sent)
            // lastRecvNetMap.put(uid, received)

            val s = "$uid : ${pm.getApplicationLabel(app)} ::-> Send : $sent, Received : $received"
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
}