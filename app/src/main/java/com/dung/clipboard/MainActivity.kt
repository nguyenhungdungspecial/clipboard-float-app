package com.dung.clipboard

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var clearAllButton: Button
    private lateinit var viewLogButton: Button
    private lateinit var starIcon: Button // Giả sử bạn có một icon ngôi sao trong activity_main.xml

    // BroadcastReceiver để nhận dữ liệu clipboard mới từ ClipboardService
    private val clipboardReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val copiedData = intent?.getStringExtra("copied_data")
            if (copiedData != null) {
                // Cập nhật giao diện của MainActivity ở đây
                // Ví dụ: myTextView.text = copiedData
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo các view
        clearAllButton = findViewById(R.id.clearAllButton)
        viewLogButton = findViewById(R.id.viewLogButton)
        starIcon = findViewById(R.id.starIcon) // Thay starIcon bằng ID thực tế của bạn

        // Xử lý sự kiện click cho các button
        clearAllButton.setOnClickListener {
            clearAll()
        }

        viewLogButton.setOnClickListener {
            // Logic để mở màn hình log
        }

        // Logic khắc phục vấn đề 1: bật/tắt cửa sổ nổi
        starIcon.setOnClickListener {
            if (isServiceRunning(FloatingWidgetService::class.java)) {
                // Nếu đang chạy, gửi lệnh để dừng nó
                val intent = Intent(this, FloatingWidgetService::class.java)
                stopService(intent)
            } else {
                // Nếu chưa chạy, khởi động nó
                val intent = Intent(this, FloatingWidgetService::class.java)
                startService(intent)
            }
        }

        // Khởi động ClipboardService để lắng nghe clipboard
        val clipboardServiceIntent = Intent(this, ClipboardService::class.java)
        startService(clipboardServiceIntent)
    }

    override fun onResume() {
        super.onResume()
        // Đăng ký receiver khi giao diện hiển thị
        val filter = IntentFilter("com.dung.clipboard.CLIPBOARD_UPDATE")
        registerReceiver(clipboardReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        // Hủy đăng ký receiver khi giao diện bị ẩn
        unregisterReceiver(clipboardReceiver)
    }

    // Hàm tiện ích để kiểm tra trạng thái của service
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    // Hàm sửa lỗi Unresolved reference: clearAll
    private fun clearAll() {
        // Logic để xóa toàn bộ dữ liệu clipboard đã lưu
        // Ví dụ: ClipboardDataManager.getInstance(this).clearAllItems()
    }
}

