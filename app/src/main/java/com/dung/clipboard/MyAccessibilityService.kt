package com.dung.clipboard

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class MyAccessibilityService : AccessibilityService() {

    private lateinit var clipboardManager: ClipboardManager

    override fun onCreate() {
        super.onCreate()
        // Khởi tạo ClipboardManager
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Dựa vào event type để lắng nghe các sự kiện liên quan đến clipboard
        // TYPE_VIEW_TEXT_SELECTION_CHANGED là một lựa chọn tốt
        // TYPE_VIEW_TEXT_CHANGED cũng là một lựa chọn tốt
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED || event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val clipItem = clipData.getItemAt(0)
                val clipText = clipItem.text
                if (!clipText.isNullOrEmpty()) {
                    // Gọi hàm addCopy của ClipboardDataManager
                    ClipboardDataManager.addCopy(clipText.toString())
                    Log.d("MyAccessibilityService", "Đã sao chép: $clipText")
                    
                    // Hiển thị một Toast để thông báo
                    Toast.makeText(applicationContext, "Đã sao chép: $clipText", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Khởi tạo ClipboardDataManager khi dịch vụ được kết nối
        ClipboardDataManager.initialize(applicationContext)
        Log.d("MyAccessibilityService", "Dịch vụ Trợ năng đã kết nối thành công.")
        Toast.makeText(applicationContext, "Dịch vụ Clipboard Nổi đã được bật", Toast.LENGTH_SHORT).show()
    }

    override fun onInterrupt() {
        Log.d("MyAccessibilityService", "Dịch vụ Trợ năng đã bị ngắt.")
    }
}

