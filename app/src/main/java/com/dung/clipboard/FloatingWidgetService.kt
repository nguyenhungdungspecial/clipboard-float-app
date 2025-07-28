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
import android.view.View // Cần import View
import androidx.core.app.NotificationCompat

class FloatingWidgetService : Service() {

    private lateinit var floatingWidget: FloatingWidget
    private val NOTIFICATION_CHANNEL_ID = "ClipboardFloatApp_Channel"
    private val NOTIFICATION_ID = 101

    private lateinit var clipboardManager: ClipboardManager // Khai báo ClipboardManager

    // Listener để lắng nghe sự thay đổi của clipboard
    private val primaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        val clipText = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
        if (!clipText.isNullOrBlank()) {
            ClipboardDataManager.addCopy(clipText)
            // Gửi broadcast để MainActivity biết và cập nhật giao diện
            // Hoặc bạn có thể gọi recreate() trực tiếp nếu MainActivity đang hiển thị
            // Nhưng cách tốt nhất là dùng BroadcastReceiver hoặc EventBus
            // Tạm thời, để đơn giản, chúng ta sẽ không gọi recreate() từ đây.
            // MainActivity sẽ tự update khi người dùng mở lại app hoặc khi recreate() được gọi từ MainActivity.
        }
    }

    override fun onCreate() {
        super.onCreate()
        ClipboardDataManager.initialize(this) // Khởi tạo ClipboardDataManager với Context
        floatingWidget = FloatingWidget(this) // Khởi tạo FloatingWidget

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(primaryClipChangedListener) // Đăng ký listener
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Thêm FLAG_UPDATE_CURRENT
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

        // Thêm OnClickListener cho floatingWidget để mở MainActivity
        floatingWidget.setOnWidgetClickListener {
            val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // Đưa Activity lên phía trước nếu đã chạy
            }
            startActivity(mainActivityIntent)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingWidget.remove() // Gỡ bỏ widget
        clipboardManager.removePrimaryClipChangedListener(primaryClipChangedListener) // Hủy đăng ký listener
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
        }
    }
}

