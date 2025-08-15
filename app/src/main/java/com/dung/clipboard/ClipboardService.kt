package com.dung.clipboard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.IBinder

/**
 * Foreground service lắng nghe clipboard và LƯU thẳng vào storage.
 * Không ép mở Activity; Activity sẽ tự cập nhật qua broadcast.
 */
class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1001, getNotification())

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            val clip = clipboardManager.primaryClip
            val txt = clip?.getItemAt(0)?.coerceToText(this)?.toString() ?: return@addPrimaryClipChangedListener
            ClipboardStorage.addItem(this, txt)
        }
    }

    private fun getNotification(): Notification {
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, "clipboard_channel")
        else Notification.Builder(this)
        b.setContentTitle("Clipboard Listener")
            .setContentText("Đang theo dõi clipboard…")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
        return b.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel("clipboard_channel", "Clipboard Service",
                    NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    override fun onBind(intent: android.content.Intent?): IBinder? = null
}
