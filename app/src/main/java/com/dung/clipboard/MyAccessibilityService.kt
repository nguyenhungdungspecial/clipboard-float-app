package com.dung.clipboard

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Bạn có thể xử lý sự kiện ở đây nếu muốn.
        // Mục đích chính: giúp app xuất hiện trong danh sách "Trợ năng".
    }

    override fun onInterrupt() {}

    companion object {
        fun isEnabled(ctx: Context): Boolean {
            return try {
                val enabled = Settings.Secure.getInt(
                    ctx.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED
                ) == 1

                if (!enabled) return false

                val settingValue = Settings.Secure.getString(
                    ctx.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: return false

                // Tên service đầy đủ theo namespace + tên class
                val expected = "${ctx.packageName}/${MyAccessibilityService::class.java.name}"
                settingValue.split(':').any { it.equals(expected, ignoreCase = true) }
            } catch (_: Exception) {
                false
            }
        }
    }
}
