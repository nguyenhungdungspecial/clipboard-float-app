package com.dung.clipboard

object ClipboardDataManager {
    private val copiedList = mutableListOf<String>()
    private val pinnedList = mutableListOf<String>()

    fun addCopy(text: String) {
        if (text.isNotBlank() && !copiedList.contains(text) && !pinnedList.contains(text)) {
            copiedList.add(0, text)
            if (copiedList.size > 20) copiedList.removeLast()
        }
    }

    fun getCopiedList(): List<String> = copiedList.toList()
    fun getPinnedList(): List<String> = pinnedList.toList()

    fun pinText(text: String) {
        if (!pinnedList.contains(text)) {
            pinnedList.add(0, text)
            copiedList.remove(text)
        }
    }

    fun unpinText(text: String) {
        pinnedList.remove(text)
    }

    fun removeText(text: String, isPinned: Boolean) {
        if (isPinned) pinnedList.remove(text) else copiedList.remove(text)
    }

    fun editText(oldText: String, newText: String, isPinned: Boolean) {
        removeText(oldText, isPinned)
        if (isPinned) pinnedList.add(0, newText) else copiedList.add(0, newText)
    }
}
