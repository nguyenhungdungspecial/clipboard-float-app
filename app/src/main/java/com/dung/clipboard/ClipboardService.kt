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
 * Foreground service lắng nghe clipboard và lưu vào ClipboardDataManager.
 * Hoạt động ngay cả khi app/Activity đang đóng.
 */
class ClipboardService : Service() {

    private lateinit var cm: ClipboardManager

    override fun onCreate() {
        super.onCreate()
        createChannelIfNeeded()
        startForeground(1001, buildNotification())

        cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.addPrimaryClipChangedListener {
            val txt = cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString() ?: return@addPrimaryClipChangedListener
            ClipboardDataManager.addItem(this, txt)
        }
    }

    private fun buildNotification(): Notification {
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, "clipboard_channel")
        else Notification.Builder(this)

        return b.setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("Clipboard Listener")
            .setContentText("Đang theo dõi clipboard…")
            .build()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(
                "clipboard_channel",
                "Clipboard Service",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(ch)
        }
    }

    override fun onBind(intent: android.content.Intent?): IBinder? = null
}
