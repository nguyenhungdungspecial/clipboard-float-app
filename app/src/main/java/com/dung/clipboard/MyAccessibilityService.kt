package com.dung.clipboard

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyAccessibilityService : AccessibilityService() {

    private val clipboardManager: ClipboardManager by lazy {
        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    }

    private val clipboardDataManager: ClipboardDataManager by lazy {
        ClipboardDataManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Dựa vào event type để lắng nghe các sự kiện liên quan đến clipboard.
        // TYPE_VIEW_TEXT_SELECTION_CHANGED là một lựa chọn tốt.
        // Bạn có thể mở rộng với các loại sự kiện khác nếu cần.
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                // Đợi một chút để đảm bảo clip đã được cập nhật
                CoroutineScope(Dispatchers.IO).launch {
                    Thread.sleep(100) // Đợi 100ms
                    val clipData = clipboardManager.primaryClip
                    if (clipData != null && clipData.itemCount > 0) {
                        val clipItem = clipData.getItemAt(0)
                        val clipText = clipItem.text
                        if (!clipText.isNullOrEmpty()) {
                            // Lưu dữ liệu đã sao chép vào ClipboardDataManager
                            clipboardDataManager.addClip(clipText.toString())
                            Log.d("MyAccessibilityService", "Đã sao chép: $clipText")

                            // Hiển thị một Toast để thông báo
                            launch(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "Đã sao chép: $clipText", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("MyAccessibilityService", "Dịch vụ Trợ năng đã kết nối thành công.")
        Toast.makeText(applicationContext, "Dịch vụ Clipboard Nổi đã được bật", Toast.LENGTH_SHORT).show()
    }

    override fun onInterrupt() {
        Log.d("MyAccessibilityService", "Dịch vụ Trợ năng đã bị ngắt.")
    }
}

