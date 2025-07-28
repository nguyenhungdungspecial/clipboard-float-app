package com.dung.clipboard

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson // Cần thêm thư viện Gson
import com.google.gson.reflect.TypeToken

object ClipboardDataManager {

    // Khai báo SharedPreferences và Gson
    private lateinit var sharedPreferences: SharedPreferences
    private val GSON = Gson()

    // Tên key cho SharedPreferences
    private const val PREFS_NAME = "ClipboardAppPrefs"
    private const val COPIED_LIST_KEY = "copied_list"
    private const val PINNED_LIST_KEY = "pinned_list"

    private val copiedList = mutableListOf<String>()
    private val pinnedList = mutableListOf<String>()

    // Hàm khởi tạo để thiết lập Context và tải dữ liệu
    fun initialize(context: Context) {
        if (!::sharedPreferences.isInitialized) { // Chỉ khởi tạo một lần
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadData()
        }
    }

    // Tải dữ liệu từ SharedPreferences
    private fun loadData() {
        val copiedJson = sharedPreferences.getString(COPIED_LIST_KEY, null)
        if (copiedJson != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            copiedList.addAll(GSON.fromJson(copiedJson, type))
        }

        val pinnedJson = sharedPreferences.getString(PINNED_LIST_KEY, null)
        if (pinnedJson != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            pinnedList.addAll(GSON.fromJson(pinnedJson, type))
        }
    }

    // Lưu dữ liệu vào SharedPreferences
    private fun saveData() {
        val editor = sharedPreferences.edit()
        editor.putString(COPIED_LIST_KEY, GSON.toJson(copiedList))
        editor.putString(PINNED_LIST_KEY, GSON.toJson(pinnedList))
        editor.apply()
    }

    fun addCopy(text: String) {
        if (text.isNotBlank() && !copiedList.contains(text) && !pinnedList.contains(text)) {
            copiedList.add(0, text)
            if (copiedList.size > 20) copiedList.removeLast()
            saveData() // Lưu dữ liệu sau mỗi lần thay đổi
        }
    }

    fun getCopiedList(): List<String> = copiedList.toList()
    fun getPinnedList(): List<String> = pinnedList.toList()

    fun pinText(text: String) {
        if (!pinnedList.contains(text)) {
            pinnedList.add(0, text)
            copiedList.remove(text)
            saveData() // Lưu dữ liệu sau mỗi lần thay đổi
        }
    }

    fun unpinText(text: String) {
        pinnedList.remove(text)
        saveData() // Lưu dữ liệu sau mỗi lần thay đổi
    }

    fun removeText(text: String, isPinned: Boolean) {
        if (isPinned) pinnedList.remove(text) else copiedList.remove(text)
        saveData() // Lưu dữ liệu sau mỗi lần thay đổi
    }

    fun editText(oldText: String, newText: String, isPinned: Boolean) {
        removeText(oldText, isPinned)
        if (isPinned) pinnedList.add(0, newText) else copiedList.add(0, newText)
        saveData() // Lưu dữ liệu sau mỗi lần thay đổi
    }
}

