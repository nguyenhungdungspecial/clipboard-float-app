package com.dung.clipboard

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Optional: inspect events if needed for advanced capture
    }

    override fun onInterrupt() { }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i("MyAccessibility", "Service connected")
    }
}
