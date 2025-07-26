package com.dung.clipboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 64, 32, 64)
        }

        val copiedTitle = TextView(this).apply {
            text = "Đã copy"
            textSize = 18f
            setBackgroundColor(0xFFB3E5FC.toInt()) // Xanh nhẹ
        }

        val pinnedTitle = TextView(this).apply {
            text = "Đã ghim"
            textSize = 18f
            setBackgroundColor(0xFFB2DFDB.toInt()) // Xanh ngọc
        }

        layout.addView(copiedTitle)

        ClipboardDataManager.getCopiedList().forEach { text ->
            val item = TextView(this).apply {
                setText(text)
                setPadding(16, 16, 16, 16)
                setOnClickListener {
                    val result = Intent().apply {
                        putExtra("pasted_text", text)
                    }
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
            }
            layout.addView(item)
        }

        layout.addView(pinnedTitle)

        ClipboardDataManager.getPinnedList().forEach { text ->
            val item = TextView(this).apply {
                setText(text)
                setPadding(16, 16, 16, 16)
                setOnClickListener {
                    val result = Intent().apply {
                        putExtra("pasted_text", text)
                    }
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
            }
            layout.addView(item)
        }

        setContentView(layout)
    }
}
