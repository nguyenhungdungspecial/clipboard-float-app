package com.dung.clipboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class FloatingWidgetService : Service() {

    private lateinit var floatingWidget: FloatingWidget
    private val NOTIFICATION_CHANNEL_ID = "ClipboardFloatApp_Channel"
    private val NOTIFICATION_ID = 101

    private lateinit var clipboardManager: ClipboardManager

    private val primaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        Log.d("FloatingWidgetService", "Clipboard changed detected!")
        val clipText = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
        if (!clipText.isNullOrBlank()) {
            Log.d("FloatingWidgetService", "New clip text: $clipText")
            if (!ClipboardDataManager.getPinnedList().contains(clipText)) {
                ClipboardDataManager.addCopy(clipText)
                Log.d("FloatingWidgetService", "Added clip to data manager.")

                val updateIntent = Intent("com.dung.clipboard.ACTION_UPDATE_UI")
                sendBroadcast(updateIntent)
                Log.d("FloatingWidgetService", "Sent broadcast to update UI.")
            } else {
                 Log.d("FloatingWidgetService", "Clip text is already pinned, not adding to copied list.")
            }
        } else {
            Log.d("FloatingWidgetService", "Clip text is null or blank.")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("FloatingWidgetService", "onCreate: Service created")
        ClipboardDataManager.initialize(this)
        floatingWidget = FloatingWidget(this)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(primaryClipChangedListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FloatingWidgetService", "onStartCommand: Service started")
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Clipboard Float App đang chạy")
            .setContentText("Chạm để mở ứng dụng quản lý clipboard")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        // Khởi động service ở chế độ foreground
        startForeground(NOTIFICATION_ID, notification)

        floatingWidget.show()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FloatingWidgetService", "onDestroy: Service destroyed")
        floatingWidget.remove()
        clipboardManager.removePrimaryClipChangedListener(primaryClipChangedListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Clipboard Float App Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            Log.d("FloatingWidgetService", "Notification Channel created")
        }
    }
}

