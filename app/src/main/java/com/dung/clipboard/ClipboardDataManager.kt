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

    private var copiedList = mutableListOf<String>()
    private var pinnedList = mutableListOf<String>()

    fun initialize(context: Context) {
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            Log.d("ClipboardDataManager", "initialize: SharedPreferences initialized")
            loadData()
        }
    }

    private fun loadData() {
        copiedList.clear()
        pinnedList.clear()
        
        val copiedJson = sharedPreferences.getString(COPIED_LIST_KEY, null)
        if (copiedJson != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            copiedList = GSON.fromJson(copiedJson, type) ?: mutableListOf()
            Log.d("ClipboardDataManager", "loadData: Loaded copied list. Size: ${copiedList.size}")
        } else {
            Log.d("ClipboardDataManager", "loadData: No copied list found in SharedPreferences.")
        }

        val pinnedJson = sharedPreferences.getString(PINNED_LIST_KEY, null)
        if (pinnedJson != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            pinnedList = GSON.fromJson(pinnedJson, type) ?: mutableListOf()
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
        if (text.isNotBlank()) {
            if (pinnedList.contains(text)) {
                Log.d("ClipboardDataManager", "addCopy: '$text' is already pinned, not adding to copied list.")
                return
            }
            if (copiedList.contains(text)) {
                // Đưa mục đã tồn tại lên đầu danh sách
                copiedList.remove(text)
            }
            copiedList.add(0, text)
            // Giới hạn danh sách chỉ 20 mục
            if (copiedList.size > 20) {
                copiedList.removeLast()
            }
            saveData()
            Log.d("ClipboardDataManager", "addCopy: Added '$text'. New copied size: ${copiedList.size}")
        } else {
            Log.d("ClipboardDataManager", "addCopy: Did not add '$text' (blank).")
        }
    }

    fun getCopiedList(): List<String> = copiedList.toList()
    fun getPinnedList(): List<String> = pinnedList.toList()

    fun pinText(text: String) {
        if (!pinnedList.contains(text)) {
            // Xóa khỏi danh sách copy trước khi ghim
            copiedList.remove(text)
            pinnedList.add(0, text)
            saveData()
            Log.d("ClipboardDataManager", "pinText: Pinned '$text'.")
        }
    }

    fun unpinText(text: String) {
        if (pinnedList.contains(text)) {
            pinnedList.remove(text)
            // Thêm lại vào danh sách đã copy nếu nó chưa tồn tại ở đó
            if (!copiedList.contains(text)) {
                copiedList.add(0, text)
                // Đảm bảo danh sách không vượt quá giới hạn
                if (copiedList.size > 20) {
                    copiedList.removeLast()
                }
            }
            saveData()
            Log.d("ClipboardDataManager", "unpinText: Unpinned '$text'.")
        }
    }

    fun removeText(text: String, isPinned: Boolean) {
        if (isPinned) {
            pinnedList.remove(text)
        } else {
            copiedList.remove(text)
        }
        saveData()
        Log.d("ClipboardDataManager", "removeText: Removed '$text'. Is pinned: $isPinned")
    }

    fun editText(oldText: String, newText: String, isPinned: Boolean) {
        if (isPinned) {
            val index = pinnedList.indexOf(oldText)
            if (index != -1) {
                pinnedList[index] = newText
            }
        } else {
            val index = copiedList.indexOf(oldText)
            if (index != -1) {
                copiedList[index] = newText
            }
        }
        saveData()
        Log.d("ClipboardDataManager", "editText: Edited from '$oldText' to '$newText'. Is pinned: $isPinned")
    }
}

