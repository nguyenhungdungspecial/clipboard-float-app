package com.dung.clipboard

object ClipboardDataManager {
    val copiedItems = mutableListOf<String>()
    val pinnedItems = mutableListOf<String>()

    fun addCopy(text: String) {
        copiedItems.add(0, text)
    }

    fun pinLast() {
        copiedItems.firstOrNull()?.let {
            if (!pinnedItems.contains(it)) pinnedItems.add(0, it)
        }
    }
}
