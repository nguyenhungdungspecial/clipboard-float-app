package com.dung.clipboard

import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log

class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager

    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString() ?: ""
                Log.d("ClipboardService", "Copied: $text")
                FloatingWidget.updateClipboardText(text)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
