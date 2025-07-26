package com.dung.clipboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
            setPadding(16, 16, 16, 16)
        }

        // Cột trái - Đã copy
        val leftColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        }

        val copiedTitle = TextView(this).apply {
            text = "Đã copy"
            textSize = 20f
            gravity = Gravity.CENTER
            setBackgroundColor(0xFFB3E5FC.toInt()) // Xanh nhạt
            setPadding(8, 16, 8, 16)
        }
        leftColumn.addView(copiedTitle)

        ClipboardDataManager.getCopiedList().forEach { text ->
            val item = createItemView(text, isPinned = false)
            leftColumn.addView(item)
        }

        // Cột phải - Đã ghim
        val rightColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        }

        val pinnedTitle = TextView(this).apply {
            text = "Đã ghim"
            textSize = 20f
            gravity = Gravity.CENTER
            setBackgroundColor(0xFFB2DFDB.toInt()) // Xanh ngọc
            setPadding(8, 16, 8, 16)
        }
        rightColumn.addView(pinnedTitle)

        ClipboardDataManager.getPinnedList().forEach { text ->
            val item = createItemView(text, isPinned = true)
            rightColumn.addView(item)
        }

        rootLayout.addView(leftColumn)
        rootLayout.addView(rightColumn)

        setContentView(rootLayout)
    }

    private fun createItemView(text: String, isPinned: Boolean): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.drawable.editbox_background)
            setOnClickListener {
                val result = Intent().apply {
                    putExtra("pasted_text", text)
                }
                setResult(Activity.RESULT_OK, result)
                finish()
            }
            setOnLongClickListener {
                showOptionsDialog(text, isPinned)
                true
            }
        }
    }

    private fun showOptionsDialog(text: String, isPinned: Boolean) {
        val builder = AlertDialog.Builder(this)
        val options = mutableListOf<String>()

        if (isPinned) {
            options.add("Bỏ ghim")
        } else {
            options.add("Ghim")
        }

        options.add("Chỉnh sửa")
        options.add("Xoá")

        builder.setTitle("Chọn thao tác")
        builder.setItems(options.toTypedArray()) { dialog, which ->
            when (options[which]) {
                "Ghim" -> {
                    ClipboardDataManager.pin(text)
                    Toast.makeText(this, "Đã ghim", Toast.LENGTH_SHORT).show()
                    recreate()
                }
                "Bỏ ghim" -> {
                    ClipboardDataManager.unpin(text)
                    Toast.makeText(this, "Đã bỏ ghim", Toast.LENGTH_SHORT).show()
                    recreate()
                }
                "Chỉnh sửa" -> {
                    showEditDialog(text, isPinned)
                }
                "Xoá" -> {
                    if (isPinned) ClipboardDataManager.unpin(text)
                    else ClipboardDataManager.removeCopied(text)
                    Toast.makeText(this, "Đã xoá", Toast.LENGTH_SHORT).show()
                    recreate()
                }
            }
        }
        builder.show()
    }

    private fun showEditDialog(oldText: String, isPinned: Boolean) {
        val input = EditText(this).apply {
            setText(oldText)
        }

        AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa nội dung")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val newText = input.text.toString()
                if (isPinned) {
                    ClipboardDataManager.unpin(oldText)
                    ClipboardDataManager.pin(newText)
                } else {
                    ClipboardDataManager.removeCopied(oldText)
                    ClipboardDataManager.addCopy(newText)
                }
                Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show()
                recreate()
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }
}
