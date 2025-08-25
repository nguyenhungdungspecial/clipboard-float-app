package com.dung.clipboard.monitor

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.dung.clipboard.MainActivity

class ClipboardMonitor(private val context: Context) : ClipboardManager.OnPrimaryClipChangedListener {

    private val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun start() {
        cm.addPrimaryClipChangedListener(this)
    }

    fun stop() {
        cm.removePrimaryClipChangedListener(this)
    }

    override fun onPrimaryClipChanged() {
        val clip = cm.primaryClip ?: return
        if (!clip.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) &&
            !clip.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) return

        val text = clip.getItemAt(0)?.coerceToText(context)?.toString()?.trim().orEmpty()
        if (text.isBlank()) return

        // Gửi broadcast để cập nhật cả MainActivity và FloatingContentService
        val broadcastIntent = Intent(MainActivity.ACTION_CLIPBOARD_UPDATED)
        context.sendBroadcast(broadcastIntent)
    }
}
