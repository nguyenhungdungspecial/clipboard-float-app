package com.dung.clipboard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder

class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1001, getNotification())

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            val clip = clipboardManager.primaryClip
            val item = clip?.getItemAt(0)?.coerceToText(this)?.toString()
            if (!item.isNullOrEmpty()) {
                // Send intent to activity (will start or deliver to running activity)
                val intent = Intent(this, ClipboardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.putExtra("new_clip", item)
                startActivity(intent)
            }
        }
    }

    private fun getNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "clipboard_channel")
        } else {
            Notification.Builder(this)
        }
        builder.setContentTitle("Clipboard Listener")
            .setContentText("Đang theo dõi clipboard")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel("clipboard_channel", "Clipboard Service", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
