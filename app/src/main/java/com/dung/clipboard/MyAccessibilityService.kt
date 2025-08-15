package com.dung.clipboard

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Có mục đích chính: để app xuất hiện trong Settings -> Accessibility
 * (Service không cần làm gì thêm cho tính năng copy hiện tại).
 */
class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* no-op */ }
    override fun onInterrupt() { /* no-op */ }
}
