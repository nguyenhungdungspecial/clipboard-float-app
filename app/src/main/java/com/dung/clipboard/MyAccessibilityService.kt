package com.dung.clipboard

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Mục tiêu chính: giúp app xuất hiện trong phần Trợ năng
 * (không bắt buộc để đọc clipboard, vì đã có ClipboardService).
 */
class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
