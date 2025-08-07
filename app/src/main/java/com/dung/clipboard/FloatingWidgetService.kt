package com.dung.clipboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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

    override fun onCreate() {
        super.onCreate()
        Log.d("FloatingWidgetService", "onCreate: Service created")
        
        // Khởi tạo FloatingWidget ở đây
        floatingWidget = FloatingWidget(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FloatingWidgetService", "onStartCommand: Service started")
        
        // Tạo notification cho foreground service
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

        // Hiển thị widget nổi
        floatingWidget.show()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FloatingWidgetService", "onDestroy: Service destroyed")
        
        // Loại bỏ widget khỏi màn hình
        floatingWidget.remove()
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

