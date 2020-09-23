package com.dm.servicedemo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

const val CHANNEL_DEFAULT_IMPORTANCE = "service channel"

class MainActivity : AppCompatActivity() {

    private lateinit var binder: MusicService.MusicBinder
    private var bound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service != null) {
                binder = service as MusicService.MusicBinder
                bound = true
                updatePlayer()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()

        playBtn.setOnClickListener {
            if (bound) {
                binder.play()
                updatePlayer()
            } else {
                val intent = Intent(this, MusicService::class.java)
                startService(intent)
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }

        pauseBtn.setOnClickListener {
            if (bound) {
                binder.pause()
                updatePlayer()
            }
        }
    }

    private fun updatePlayer() {
        if (binder.isPlaying) {
            playBtn.isEnabled = false
            pauseBtn.isEnabled = true
        } else {
            playBtn.isEnabled = true
            pauseBtn.isEnabled = false
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, connection, 0)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        bound = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    CHANNEL_DEFAULT_IMPORTANCE,
                    "Service channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for foreground services"
            }
            val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}