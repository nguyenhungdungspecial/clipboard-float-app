package com.dung.clipboard.monitor

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import com.dung.clipboard.FloatingWidgetService

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

        // lưu vào "copied"
        val prefs = context.getSharedPreferences("clipboard_store", Context.MODE_PRIVATE)
        val raw = prefs.getString("copied", "") ?: ""
        val list = if (raw.isBlank()) mutableListOf() else raw.split("\u0001").toMutableList()
        list.remove(text)
        list.add(0, text)
        prefs.edit().putString("copied", list.joinToString("\u0001")).apply()

        // cập nhật floating widget
        FloatingWidgetService.updateClipboardText(text)
    }
}

