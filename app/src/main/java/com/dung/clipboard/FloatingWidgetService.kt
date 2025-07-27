package com.dung.clipboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class FloatingWidgetService : Service() {

    private lateinit var floatingWidget: FloatingWidget
    private val NOTIFICATION_CHANNEL_ID = "ClipboardFloatApp_Channel"
    private val NOTIFICATION_ID = 101

    override fun onCreate() {
        super.onCreate()
        floatingWidget = FloatingWidget(this) // Khởi tạo FloatingWidget
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Tạo Notification Channel (chỉ cần cho Android Oreo trở lên)
        createNotificationChannel()

        // Tạo Intent để mở MainActivity khi click vào notification
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE là bắt buộc cho PendingIntent từ Android S trở đi
        )

        // Tạo Notification để chạy Foreground Service
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Clipboard Float App đang chạy")
            .setContentText("Chạm để mở ứng dụng quản lý clipboard")
            .setSmallIcon(android.R.drawable.ic_menu_edit) // Bạn có thể thay bằng icon app của mình
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Không thể vuốt bỏ notification
            .build()

        // Bắt đầu Foreground Service
        startForeground(NOTIFICATION_ID, notification)

        // Hiển thị Floating Widget
        // Kiểm tra quyền vẽ đè ở đây là không cần thiết nếu đã kiểm tra ở MainActivity trước khi khởi động Service
        // Vì dịch vụ chạy ngầm, Toast sẽ không hiển thị.
        floatingWidget.show()


        return START_STICKY // Nếu service bị kill, hệ thống sẽ cố gắng khởi động lại nó
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingWidget.remove() // Gọi phương thức remove() để ẩn widget khi service bị hủy
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Không hỗ trợ binding
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
        }
    }
}

