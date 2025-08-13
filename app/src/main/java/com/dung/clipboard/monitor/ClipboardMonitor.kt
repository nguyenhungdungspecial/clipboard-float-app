package com.dung.clipboard.monitor

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import com.dung.clipboard.FloatingWidget

class ClipboardMonitor(context: Context) {
    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    init {
        clipboard.addPrimaryClipChangedListener {
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString() ?: ""
                Log.d("ClipboardMonitor", "Detected: $text")
                FloatingWidget.updateClipboardText(text)
            }
        }
    }
}
