package com.dung.clipboard

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            val copiedText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (!copiedText.isNullOrBlank()) {
                ClipboardDataManager.addCopy(copiedText)
                recreate()
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        val copiedLayout = createColumn("Đã copy", ClipboardDataManager.getCopiedList(), false)
        val pinnedLayout = createColumn("Đã ghim", ClipboardDataManager.getPinnedList(), true)

        layout.addView(copiedLayout)

        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                setMargins(4, 0, 4, 0)
            }
            setBackgroundColor(Color.DKGRAY)
        }

        layout.addView(divider)
        layout.addView(pinnedLayout)

        setContentView(layout)
    }

    private fun createColumn(title: String, list: List<String>, isPinned: Boolean): LinearLayout {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 18f
            setBackgroundColor(if (isPinned) 0xFFB2DFDB.toInt() else 0xFFB3E5FC.toInt())
            setPadding(16, 16, 16, 16)
        }
        column.addView(titleView)

        list.forEach { originalText ->
            val item = createTextItem(originalText, isPinned)
            column.addView(item)
        }

        return column
    }

    private fun createTextItem(originalText: String, isPinned: Boolean): LinearLayout
