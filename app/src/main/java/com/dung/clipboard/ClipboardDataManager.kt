package com.dung.clipboard

import android.content.Context

object ClipboardDataManager {
    private const val PREFS = "clipboard_prefs"
    private const val KEY_COPIED = "copied_list"
    private const val KEY_PINNED = "pinned_list"

    private val inMemory = mutableListOf<String>()
    private val pinned = mutableListOf<String>()

    fun addItem(ctx: Context, text: String) {
        synchronized(this) {
            if (inMemory.isNotEmpty() && inMemory[0] == text) return
            inMemory.add(0, text)
            if (inMemory.size > 200) inMemory.removeAt(inMemory.size - 1)
            saveToPrefs(ctx)
        }
    }

    fun getCopiedList(ctx: Context): List<String> {
        if (inMemory.isEmpty()) loadFromPrefs(ctx)
        return inMemory.toList()
    }

    fun getPinnedList(ctx: Context): List<String> {
        if (pinned.isEmpty()) loadFromPrefs(ctx)
        return pinned.toList()
    }

    fun pinItem(ctx: Context, text: String) {
        if (!pinned.contains(text)) {
            pinned.add(0, text)
            saveToPrefs(ctx)
        }
    }

    fun unpinItem(ctx: Context, text: String) {
        if (pinned.remove(text)) saveToPrefs(ctx)
    }

    private fun saveToPrefs(ctx: Context) {
        val shared = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        shared.edit().putString(KEY_COPIED, inMemory.joinToString("\n"))
            .putString(KEY_PINNED, pinned.joinToString("\n"))
            .apply()
    }

    private fun loadFromPrefs(ctx: Context) {
        val shared = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = shared.getString(KEY_COPIED, "") ?: ""
        val pr = shared.getString(KEY_PINNED, "") ?: ""
        inMemory.clear()
        pinned.clear()
        if (raw.isNotEmpty()) inMemory.addAll(raw.split('\n').filter { it.isNotEmpty() })
        if (pr.isNotEmpty()) pinned.addAll(pr.split('\n').filter { it.isNotEmpty() })
    }
}
