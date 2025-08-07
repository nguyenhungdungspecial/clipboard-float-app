package com.dung.clipboard

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ClipboardManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import android.os.Build
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

private const val TAG = "ClipboardService"
private const val NOTIFICATION_CHANNEL_ID = "clipboard_channel_id"
private const val NOTIFICATION_ID = 1

class ClipboardService : Service() {
    private lateinit var clipboardManager: ClipboardManager
    private var isFloatingWidgetServiceRunning = false

    // Listener để lắng nghe sự thay đổi của clipboard
    private val clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        try {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val item = clipData.getItemAt(0)
                val copiedText = item.text?.toString()

                if (!copiedText.isNullOrEmpty()) {
                    Log.d(TAG, "Đã copy: $copiedText")

                    // Thêm dữ liệu vào ClipboardDataManager
                    ClipboardDataManager.addCopy(copiedText)

                    // Hiển thị Toast thông báo
                    Toast.makeText(this, "Đã copy: $copiedText", Toast.LENGTH_SHORT).show()

                    // KIỂM TRA VÀ KHỞI ĐỘNG FLOATINGWIDGETSERVICE NẾU CHƯA CHẠY
                    if (!isFloatingWidgetServiceRunning) {
                        startFloatingWidgetService()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi xử lý clipboard: ${e.message}")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service đã được khởi tạo")

        // Khởi tạo ClipboardDataManager trước khi sử dụng
        ClipboardDataManager.initialize(applicationContext)

        // Khởi tạo ClipboardManager và đăng ký listener
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(clipChangedListener)

        // Bắt đầu dịch vụ tiền cảnh (Foreground Service)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service đã bị hủy")

        // Hủy đăng ký listener khi service bị hủy
        if (::clipboardManager.isInitialized) {
            clipboardManager.removePrimaryClipChangedListener(clipChangedListener)
        }

        // Dừng FloatingWidgetService khi ClipboardService bị hủy
        stopFloatingWidgetService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startFloatingWidgetService() {
        val serviceIntent = Intent(this, FloatingWidgetService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        isFloatingWidgetServiceRunning = true
        Log.d(TAG, "FloatingWidgetService đã được khởi động.")
    }

    private fun stopFloatingWidgetService() {
        val serviceIntent = Intent(this, FloatingWidgetService::class.java)
        stopService(serviceIntent)
        isFloatingWidgetServiceRunning = false
        Log.d(TAG, "FloatingWidgetService đã được dừng.")
    }

    private fun createNotification(): Notification {
        val channelName = "Clipboard Float Service"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Clipboard Float App")
            .setContentText("Đang lắng nghe dữ liệu clipboard...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}

