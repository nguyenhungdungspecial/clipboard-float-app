package com.dung.clipboard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.dung.clipboard.monitor.ClipboardMonitor

class ClipboardService : Service() {

    private lateinit var clipboardMonitor: ClipboardMonitor

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1001, getNotification())

        clipboardMonitor = ClipboardMonitor(this)
        clipboardMonitor.start()
    }

    private fun getNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, "clipboard_channel")
        else Notification.Builder(this)

        builder.setContentTitle("Clipboard Listener")
            .setContentText("Đang theo dõi clipboard…")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                "clipboard_channel",
                "Clipboard Service",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        clipboardMonitor.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

