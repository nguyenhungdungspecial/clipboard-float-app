package com.dung.clipboard.utils

import android.content.Context
import android.content.SharedPreferences

private const val PREFS = "clipboard_prefs"
private const val KEY_COPIED = "copied_list"
private const val KEY_PINNED = "pinned_list"

object Utils {

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getCopiedList(ctx: Context): MutableList<String> {
        val set = prefs(ctx).getStringSet(KEY_COPIED, emptySet()) ?: emptySet()
        return set.toMutableList().sortedByDescending { it }.toMutableList()
    }

    fun getPinnedList(ctx: Context): MutableList<String> {
        val set = prefs(ctx).getStringSet(KEY_PINNED, emptySet()) ?: emptySet()
        return set.toMutableList().sorted().toMutableList()
    }

    fun addCopied(ctx: Context, value: String) {
        if (value.isBlank()) return
        val p = prefs(ctx)
        val cur = p.getStringSet(KEY_COPIED, mutableSetOf())!!.toMutableSet()
        cur.add(value)
        p.edit().putStringSet(KEY_COPIED, cur).apply()
    }

    fun pin(ctx: Context, value: String) {
        if (value.isBlank()) return
        val p = prefs(ctx)
        val cur = p.getStringSet(KEY_PINNED, mutableSetOf())!!.toMutableSet()
        cur.add(value)
        p.edit().putStringSet(KEY_PINNED, cur).apply()
    }

    fun unpin(ctx: Context, value: String) {
        val p = prefs(ctx)
        val cur = p.getStringSet(KEY_PINNED, mutableSetOf())!!.toMutableSet()
        cur.remove(value)
        p.edit().putStringSet(KEY_PINNED, cur).apply()
    }

    fun clearCopied(ctx: Context) {
        prefs(ctx).edit().putStringSet(KEY_COPIED, emptySet()).apply()
    }
}
