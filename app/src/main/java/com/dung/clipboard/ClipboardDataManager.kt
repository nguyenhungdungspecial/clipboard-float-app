package com.dung.clipboard

import android.content.Context
import android.content.Intent

/**
 * Lưu lịch sử clipboard vào SharedPreferences (giữ thứ tự, giới hạn 200 mục)
 * và phát broadcast để UI cập nhật.
 */
object ClipboardDataManager {
    private const val PREF = "clipboard_data"
    private const val KEY = "items_joined"
    private const val SEP = "\u0001" // ít gặp trong text người dùng

    fun getItems(ctx: Context): MutableList<String> {
        val joined = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, "") ?: ""
        if (joined.isEmpty()) return mutableListOf()
        return joined.split(SEP).filter { it.isNotBlank() }.toMutableList()
    }

    fun saveItems(ctx: Context, list: List<String>) {
        val joined = list.joinToString(SEP)
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, joined).apply()
        ctx.sendBroadcast(Intent(ACTION_ITEMS_UPDATED))
    }

    fun addItem(ctx: Context, text: String, max: Int = 200) {
        val t = text.trim()
        if (t.isEmpty()) return
        val list = getItems(ctx)
        // tránh trùng mục mới nhất
        if (list.firstOrNull() == t) return
        list.add(0, t)
        while (list.size > max) list.removeLast()
        saveItems(ctx, list)
    }

    fun clear(ctx: Context) {
        saveItems(ctx, emptyList())
    }
}

/** Broadcast actions dùng chung giữa Service & Activity */
const val ACTION_ITEMS_UPDATED = "com.dung.clipboard.ACTION_ITEMS_UPDATED"
const val ACTION_CLOSE_ACTIVITY = "com.dung.clipboard.ACTION_CLOSE_ACTIVITY"
