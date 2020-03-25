package com.fullsekurity.remoteserviceserver

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Chronometer
import java.lang.ref.WeakReference

class BoundService : Service() {
    private var mChronometer: Chronometer? = null
    private val mMessenger = Messenger(BoundServiceHandler(this))

    internal class BoundServiceHandler(service: BoundService) : Handler() {
        private val mService = WeakReference(service)
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_GET_TIMESTAMP -> {
                    mService.get()?. let {
                        val elapsedMillis = (SystemClock.elapsedRealtime() - mService.get()?.mChronometer!!.base)
                        val hours = (elapsedMillis / 3600000).toInt()
                        val minutes = (elapsedMillis - hours * 3600000).toInt() / 60000
                        val seconds = (elapsedMillis - hours * 3600000 - (minutes * 60000)).toInt() / 1000
                        val millis = (elapsedMillis - hours * 3600000 - (minutes * 60000) - seconds * 1000).toInt()
                        val activityMessenger = msg.replyTo
                        val b = Bundle()
                        b.putString("timestamp", "$hours:$minutes:$seconds:$millis")
                        val replyMsg = Message.obtain(null, MSG_GET_TIMESTAMP)
                        replyMsg.data = b
                        try {
                            activityMessenger.send(replyMsg)
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        mChronometer = Chronometer(this)
        mChronometer?.base = SystemClock.elapsedRealtime()
        mChronometer?.start()
    }

    override fun onBind(intent: Intent): IBinder {
        return mMessenger.binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mChronometer?.stop()
    }

    companion object {
        const val MSG_GET_TIMESTAMP = 1000
    }
}