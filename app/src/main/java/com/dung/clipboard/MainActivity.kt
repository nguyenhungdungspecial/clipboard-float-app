package com.dung.clipboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 64, 32, 64)
        }

        val copiedTitle = TextView(this).apply {
            text = "Đã copy"
            textSize = 20f
            setPadding(0, 16, 0, 8)
            setBackgroundColor(0xFFB3E5FC.toInt())
        }
        layout.addView(copiedTitle)

        ClipboardDataManager.getCopiedList().forEach { text ->
            val item = createTextItem(text, isPinned = false)
            layout.addView(item)
        }

        val pinnedTitle = TextView(this).apply {
            text = "Đã ghim"
            textSize = 20f
            setPadding(0, 32, 0, 8)
            setBackgroundColor(0xFFB2DFDB.toInt())
        }
        layout.addView(pinnedTitle)

        ClipboardDataManager.getPinnedList().forEach { text ->
            val item = createTextItem(text, isPinned = true)
            layout.addView(item)
        }

        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun createTextItem(text: String, isPinned: Boolean): LinearLayout {
        val context = this

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            background = ContextCompat.getDrawable(context, android.R.drawable.dialog_holo_light_frame)
        }

        val textView = TextView(context).apply {
            setText(text)
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                val result = Intent().apply {
                    putExtra("pasted_text", text)
                }
                setResult(Activity.RESULT_OK, result)
                finish()
            }
        }

        val pinBtn = Button(context).apply {
            text = if (isPinned) "Bỏ ghim" else "Ghim"
            setOnClickListener {
                if (isPinned) {
                    ClipboardDataManager.unpin(text)
                } else {
                    ClipboardDataManager.pin(text)
                }
                recreate() // refresh lại giao diện
            }
        }

        container.addView(textView)
        container.addView(pinBtn)
        return container
    }
}
