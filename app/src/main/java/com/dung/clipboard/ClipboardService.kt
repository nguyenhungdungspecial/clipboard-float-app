package com.dung.clipboard

import android.app.Service
import android.content.*
import android.os.IBinder
import android.content.ClipboardManager
import android.widget.Toast

class ClipboardService : Service() {
    private lateinit var clipboard: ClipboardManager

    override fun onCreate() {
        super.onCreate()
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            val text = clipboard.primaryClip?.getItemAt(0)?.text.toString()
            ClipboardDataManager.addCopy(text)
            Toast.makeText(this, "Đã copy: $text", Toast.LENGTH_SHORT).show()
        }
        FloatingWidget(this).show()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
