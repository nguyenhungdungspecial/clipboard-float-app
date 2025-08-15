package com.dung.clipboard

import android.content.Context

/**
 * Lưu & đọc danh sách clipboard bằng SharedPreferences.
 * Lưu dạng một chuỗi nối bằng ký tự đặc biệt '\u0001' để giữ thứ tự.
 */
object ClipboardStorage {
    private const val PREF = "clipboard_data"
    private const val KEY = "items_joined"
    private const val SEP = "\u0001"

    fun getItems(ctx: Context): MutableList<String> {
        val all = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, "") ?: ""
        if (all.isEmpty()) return mutableListOf()
        return all.split(SEP).filter { it.isNotBlank() }.toMutableList()
    }

    fun saveItems(ctx: Context, list: List<String>) {
        val joined = list.joinToString(SEP)
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, joined).apply()
    }

    fun addItem(ctx: Context, text: String, max: Int = 200) {
        val list = getItems(ctx)
        if (text.isBlank()) return
        if (list.isNotEmpty() && list[0] == text) return
        list.add(0, text)
        while (list.size > max) list.removeLast()
        saveItems(ctx, list)
        // phát broadcast để UI cập nhật
        ctx.sendBroadcast(android.content.Intent(ACTION_ITEMS_UPDATED))
    }

    const val ACTION_ITEMS_UPDATED = "com.dung.clipboard.ACTION_ITEMS_UPDATED"
 
   const val ACTION_CLOSE_ACTIVITY = "com.dung.clipboard.ACTION_CLOSE_ACTIVITY"
}
