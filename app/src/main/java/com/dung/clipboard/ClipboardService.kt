package com.dung.clipboard

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.text.TextUtils
import android.util.Log

class ClipboardService : Service() {

    private lateinit var clipboard: android.content.ClipboardManager
    private val listener = android.content.ClipboardManager.OnPrimaryClipChangedListener {
        try {
            val clip = clipboard.primaryClip
            val item = clip?.getItemAt(0)
            val text = item?.coerceToText(this)?.toString() ?: ""
            if (!TextUtils.isEmpty(text)) {
                ClipboardDataManager.addItem(applicationContext, text)
                val intent = Intent(MainActivity.ACTION_CLIPBOARD_UPDATED)
                sendBroadcast(intent)
            }
        } catch (e: Exception) {
            Log.e("ClipboardService", "error reading clip", e)
        }
    }

    override fun onCreate() {
        super.onCreate()
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.addPrimaryClipChangedListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        try { clipboard.removePrimaryClipChangedListener(listener) } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?) = null
}
