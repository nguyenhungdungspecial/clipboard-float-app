package com.dung.clipboard

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log

class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager

    private val onPrimaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val item = clipData.getItemAt(0)
            val copiedText = item.text?.toString()

            if (copiedText != null) {
                Log.d("ClipboardService", "New text copied: $copiedText")

                val intent = Intent("com.dung.clipboard.CLIPBOARD_UPDATE")
                intent.putExtra("copied_data", copiedText)
                sendBroadcast(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(onPrimaryClipChangedListener)
    }

    override fun onBind(intent: Intent?): I
