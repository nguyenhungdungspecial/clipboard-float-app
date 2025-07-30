package com.dung.clipboard

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ClipboardDataManager {

    private lateinit var sharedPreferences: SharedPreferences
    private val GSON = Gson()

    private const val PREFS_NAME = "ClipboardAppPrefs"
    private const val COPIED_LIST_KEY = "copied_list"
    private const val PINNED_LIST_KEY = "pinned_list"

    private val copiedList = mutableListOf<String>()
    private val pinnedList = mutableListOf<String>()

    fun initialize(context: Context) {
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            Log.d("ClipboardDataManager", "initialize: SharedPreferences initialized")
            loadData()
        }
    }

    private fun loadData() {
        val copiedJson = sharedPreferences.getString(COPIED_LIST_KEY, null)
        if (copiedJson!= null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            copiedList.addAll(GSON.fromJson(copiedJson, type))
            Log.d("ClipboardDataManager", "loadData: Loaded copied list. Size: ${copiedList.size}")
        } else {
            Log.d("ClipboardDataManager", "loadData: No copied list found in SharedPreferences.")
        }

        val pinnedJson = sharedPreferences.getString(PINNED_LIST_KEY, null)
        if (pinnedJson!= null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            pinnedList.addAll(GSON.fromJson(pinnedJson, type))
            Log.d("ClipboardDataManager", "loadData: Loaded pinned list. Size: ${pinnedList.size}")
        } else {
            Log.d("ClipboardDataManager", "loadData: No pinned list found in SharedPreferences.")
        }
    }

    private fun saveData() {
        val editor = sharedPreferences.edit()
        editor.putString(COPIED_LIST_KEY, GSON.toJson(copiedList))
        editor.putString(PINNED_LIST_KEY, GSON.toJson(pinnedList))
        editor.apply()
        Log.d("ClipboardDataManager", "saveData: Data saved. Copied size: ${copiedList.size}, Pinned size: ${pinnedList.size}")
    }

    fun addCopy(text: String) {
        if (text.isNotBlank() &&!copiedList.contains(text) &&!pinnedList.contains(text)) {
            copiedList.add(0, text)
            if (copiedList.size > 20) copiedList.removeLast()
            saveData()
            Log.d("ClipboardDataManager", "addCopy: Added '$text'. New copied size: ${copiedList.size}")
        } else {
            Log.d("ClipboardDataManager", "addCopy: Did not add '$text' (blank, duplicate, or pinned).")
        }
    }

    fun getCopiedList(): List<String> = copiedList.toList()
    fun getPinnedList(): List<String> = pinnedList.toList()

    fun pinText(text: String) {
        if (!pinnedList.contains(text)) {
            pinnedList.add(0, text)
            copiedList.remove(text)
            saveData()
            Log.d("ClipboardDataManager", "pinText: Pinned '$text'.")
        }
    }

    fun unpinText(text: String) {
        pinnedList.remove(text)
        saveData()
        Log.d("ClipboardDataManager", "unpinText: Unpinned '$text'.")
    }

    fun removeText(text: String, isPinned: Boolean) {
        if (isPinned) pinnedList.remove(text) else copiedList.remove(text)
        saveData()
        Log.d("ClipboardDataManager", "removeText: Removed '$text'. Is pinned: $isPinned")
    }

    fun editText(oldText: String, newText: String, isPinned: Boolean) {
        removeText(oldText, isPinned)
        if (isPinned) pinnedList.add(0, newText) else copiedList.add(0, newText)
        saveData()
        Log.d("ClipboardDataManager", "editText: Edited from '$oldText' to '$newText'. Is pinned: $isPinned")
    }
}

