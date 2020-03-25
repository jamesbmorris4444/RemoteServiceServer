package com.fullsekurity.remoteserviceserver

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    private var mBoundServiceMessenger: Messenger? = null
    private var mServiceConnected = false
    private var mTimestampText: TextView? = null
    private val mActivityMessenger = Messenger(
            ActivityHandler(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTimestampText = findViewById<View>(R.id.timestamp_text) as TextView
        val printTimestampButton = findViewById<View>(R.id.print_timestamp) as Button
        val stopServiceButon = findViewById<View>(R.id.stop_service) as Button
        val startServiceButon = findViewById<View>(R.id.start_service) as Button

        startServiceButon.setOnClickListener {
            val intent = Intent(this@MainActivity, BoundService::class.java)
            startService(intent)
//            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
        printTimestampButton.setOnClickListener {
            if (mServiceConnected) {
                try {
                    val msg = Message.obtain(null, BoundService.MSG_GET_TIMESTAMP)
                    msg.replyTo = mActivityMessenger
                    mBoundServiceMessenger?.send(msg)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }
        stopServiceButon.setOnClickListener {
//            if (mServiceConnected) {
//                unbindService(mServiceConnection)
//                mServiceConnected = false
//            }
            val intent = Intent(this@MainActivity, BoundService::class.java)
            stopService(intent)
        }
    }

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mBoundServiceMessenger = null
            mServiceConnected = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mBoundServiceMessenger = Messenger(service)
            mServiceConnected = true
        }
    }

    internal class ActivityHandler(activity: MainActivity) : Handler() {
        private val mActivity = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BoundService.MSG_GET_TIMESTAMP -> {
                    mActivity.get()?.mTimestampText?.text = msg.data.getString("timestamp")}
            }
        }
    }
}
