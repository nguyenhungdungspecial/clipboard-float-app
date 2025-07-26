package com.dung.clipboard

object ClipboardDataManager {
    private val copiedList = mutableListOf<String>()
    private val pinnedList = mutableListOf<String>()

    fun addCopy(text: String) {
        if (text.isNotBlank() && !copiedList.contains(text) && !pinnedList.contains(text)) {
            copiedList.add(0, text)
            if (copiedList.size > 20) copiedList.removeLast() // Giới hạn tối đa
        }
    }

    fun getCopiedList(): List<String> = copiedList.toList()
    fun getPinnedList(): List<String> = pinnedList.toList()

    fun pin(text: String) {
        if (!pinnedList.contains(text)) {
            pinnedList.add(0, text)
            copiedList.remove(text)
        }
    }

    fun unpin(text: String) {
        pinnedList.remove(text)
    }

    fun removeCopied(text: String) {
        copiedList.remove(text)
    }

    fun editCopied(oldText: String, newText: String) {
        val index = copiedList.indexOf(oldText)
        if (index != -1) {
            copiedList[index] = newText
        }
    }

    fun editPinned(oldText: String, newText: String) {
        val index = pinnedList.indexOf(oldText)
        if (index != -1) {
            pinnedList[index] = newText
        }
    }

    fun isPinned(text: String): Boolean = pinnedList.contains(text)
}
