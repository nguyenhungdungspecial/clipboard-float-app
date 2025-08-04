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
import android.widget.Toast
import androidx.core.app.NotificationCompat

class FloatingWidgetService : Service() {

    private lateinit var floatingWidget: FloatingWidget
    private val NOTIFICATION_CHANNEL_ID = "ClipboardFloatApp_Channel"
    private val NOTIFICATION_ID = 101

    private lateinit var clipboardManager: ClipboardManager
    private lateinit var fileLogger: FileLogger

    private val primaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        fileLogger.log("FloatingWidgetService", "Clipboard changed detected!")
        val clipText = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
        if (!clipText.isNullOrBlank()) {
            fileLogger.log("FloatingWidgetService", "New clip text: $clipText")
            if (!ClipboardDataManager.getPinnedList().contains(clipText)) {
                ClipboardDataManager.addCopy(clipText)
                fileLogger.log("FloatingWidgetService", "Added clip to data manager.")

                val updateIntent = Intent("com.dung.clipboard.ACTION_UPDATE_UI")
                sendBroadcast(updateIntent)
                fileLogger.log("FloatingWidgetService", "Sent broadcast to update UI.")
            } else {
                 fileLogger.log("FloatingWidgetService", "Clip text is already pinned, not adding to copied list.")
            }
        } else {
            fileLogger.log("FloatingWidgetService", "Clip text is null or blank.")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(this, "Service onCreate", Toast.LENGTH_SHORT).show()
        fileLogger = FileLogger(this)
        fileLogger.log("FloatingWidgetService", "onCreate: Service created")
        ClipboardDataManager.initialize(this)
        floatingWidget = FloatingWidget(this)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(primaryClipChangedListener)
        fileLogger.log("FloatingWidgetService", "onPrimaryClipChangedListener registered.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fileLogger.log("FloatingWidgetService", "onStartCommand: Service started")
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

        startForeground(NOTIFICATION_ID, notification)

        floatingWidget.show()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fileLogger.log("FloatingWidgetService", "onDestroy: Service destroyed")
        floatingWidget.remove()
        clipboardManager.removePrimaryClipChangedListener(primaryClipChangedListener)
        fileLogger.log("FloatingWidgetService", "onPrimaryClipChangedListener unregistered.")
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
            fileLogger.log("FloatingWidgetService", "Notification Channel created")
        }
    }
}

