package com.dung.clipboard

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class MyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event != null) {
            Log.d("AccessibilityService", "Event: ${event.eventType}")
        }
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "Service Interrupted")
    }
}
