package com.dung.clipboard

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Theo dõi clipboard hệ thống
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (!text.isNullOrBlank()) {
                ClipboardDataManager.addCopy(text)
                recreate()
            }
        }

        // Layout ngang có 2 cột và đường phân cách giữa
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        val copiedLayout = createColumn("Đã copy", ClipboardDataManager.getCopiedList(), false)
        val pinnedLayout = createColumn("Đã ghim", ClipboardDataManager.getPinnedList(), true)

        layout.addView(copiedLayout)

        // Thêm đường kẻ chia giữa
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(4, LinearLayout.LayoutParams.MATCH_PARENT)
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

        list.forEach { text ->
            val item = createTextItem(text, isPinned)
            column.addView(item)
        }

        return column
    }

    private fun createTextItem(text: String, isPinned: Boolean): LinearLayout {
        val itemLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(8, 8, 8, 8)
        }

        val textView = TextView(this).apply {
            this.text = text
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                val result = Intent().apply {
                    putExtra("pasted_text", text)
                }
                setResult(Activity.RESULT_OK, result)
                finish()
            }
        }

        val editBtn = Button(this).apply {
            text = "Sửa"
            setOnClickListener {
                val editText = EditText(this@MainActivity).apply { setText(text) }
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Sửa nội dung")
                    .setView(editText)
                    .setPositiveButton("Lưu") { _, _ ->
                        val newText = editText.text.toString()
                        if (newText.isNotBlank()) {
                            ClipboardDataManager.editText(text, newText, isPinned)
                            recreate()
                        }
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        }

        val pinBtn = Button(this).apply {
            text = if (isPinned) "Bỏ ghim" else "Ghim"
            setOnClickListener {
                if (isPinned) ClipboardDataManager.unpinText(text)
                else ClipboardDataManager.pinText(text)
                recreate()
            }
        }

        val deleteBtn = Button(this).apply {
            text = "Xoá"
            setOnClickListener {
                ClipboardDataManager.removeText(text, isPinned)
                recreate()
            }
        }

        itemLayout.addView(textView)
        itemLayout.addView(editBtn)
        itemLayout.addView(pinBtn)
        itemLayout.addView(deleteBtn)

        return itemLayout
    }
}
