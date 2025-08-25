package com.dung.clipboard

import android.content.Context
import android.util.Log

object ClipboardDataManager {
    private const val PREF_NAME = "clipboard_data"
    private const val KEY_COPIED = "copied_list"
    private const val KEY_PINNED = "pinned_list"
    private const val SEPARATOR = "\u0001"
    private const val TAG = "ClipboardDataManager"

    private val inMemoryCopied = mutableListOf<String>()
    private val inMemoryPinned = mutableListOf<String>()

    fun addItem(ctx: Context, text: String, max: Int = 200) {
        if (text.isBlank()) {
            Log.d(TAG, "Attempted to add blank text. Ignored.")
            return
        }

        synchronized(this) {
            if (inMemoryCopied.isNotEmpty() && inMemoryCopied[0] == text) {
                Log.d(TAG, "Item already exists. Not adding.")
                return
            }

            if (inMemoryCopied.contains(text)) {
                inMemoryCopied.remove(text)
                Log.d(TAG, "Duplicate item found and removed.")
            }

            inMemoryCopied.add(0, text)

            if (inMemoryCopied.size > max) {
                inMemoryCopied.removeLast()
            }
            saveToPrefs(ctx)
            Log.d(TAG, "Item added and saved: $text")
        }
    }

    fun getCopiedList(ctx: Context): List<String> {
        loadFromPrefs(ctx)
        Log.d(TAG, "Returning copied list with size: ${inMemoryCopied.size}")
        return inMemoryCopied.toList()
    }

    fun getPinnedList(ctx: Context): List<String> {
        loadFromPrefs(ctx)
        Log.d(TAG, "Returning pinned list with size: ${inMemoryPinned.size}")
        return inMemoryPinned.toList()
    }

    fun pinItem(ctx: Context, text: String) {
        if (!inMemoryPinned.contains(text)) {
            inMemoryPinned.add(0, text)
            saveToPrefs(ctx)
        }
    }

    fun unpinItem(ctx: Context, text: String) {
        if (inMemoryPinned.remove(text)) {
            saveToPrefs(ctx)
        }
    }

    fun clearAll(ctx: Context) {
        synchronized(this) {
            inMemoryCopied.clear()
            inMemoryPinned.clear()
            saveToPrefs(ctx)
        }
    }

    private fun saveToPrefs(ctx: Context) {
        val shared = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val copiedJoined = inMemoryCopied.joinToString(SEPARATOR)
        val pinnedJoined = inMemoryPinned.joinToString(SEPARATOR)
        shared.edit()
            .putString(KEY_COPIED, copiedJoined)
            .putString(KEY_PINNED, pinnedJoined)
            .apply()
        Log.d(TAG, "Data saved to SharedPreferences. Copied size: ${inMemoryCopied.size}")
    }

    private fun loadFromPrefs(ctx: Context) {
        val shared = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val rawCopied = shared.getString(KEY_COPIED, "") ?: ""
        val rawPinned = shared.getString(KEY_PINNED, "") ?: ""

        inMemoryCopied.clear()
        inMemoryPinned.clear()

        if (rawCopied.isNotEmpty()) {
            inMemoryCopied.addAll(rawCopied.split(SEPARATOR).filter { it.isNotEmpty() })
        }
        if (rawPinned.isNotEmpty()) {
            inMemoryPinned.addAll(rawPinned.split(SEPARATOR).filter { it.isNotEmpty() })
        }
        Log.d(TAG, "Data loaded from SharedPreferences. Copied size: ${inMemoryCopied.size}")
    }
}

