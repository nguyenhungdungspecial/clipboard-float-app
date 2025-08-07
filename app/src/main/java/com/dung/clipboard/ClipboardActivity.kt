package com.dung.clipboard

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.ClipData

class ClipboardActivity : AppCompatActivity() {
    private lateinit var clipboardManager: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Khởi tạo ClipboardDataManager trước khi sử dụng
        ClipboardDataManager.initialize(this)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Thiết lập layout chính của Activity
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.argb(200, 0, 150, 136))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Thêm tiêu đề
        val title = TextView(this).apply {
            text = "📋 ĐÃ COPY"
            setTextColor(Color.WHITE)
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        mainLayout.addView(title)
        
        // Thêm ScrollView để có thể cuộn danh sách
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        // Layout để chứa các item đã copy
        val itemsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
            }
        }

        // Thêm các item đã copy vào layout
        addCopiedItems(itemsLayout)

        scrollView.addView(itemsLayout)
        mainLayout.addView(scrollView)
        setContentView(mainLayout)
    }

    private fun addCopiedItems(layout: LinearLayout) {
        ClipboardDataManager.getCopiedList().forEach { text ->
            val textView = TextView(this).apply {
                this.text = text
                setTextColor(Color.BLACK)
                textSize = 16f
                setBackgroundColor(Color.WHITE)
                setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(8))
                }
                // Khi click vào item, sao chép lại nội dung vào clipboard
                setOnClickListener {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("Copied Text", text))
                    Toast.makeText(this@ClipboardActivity, "Đã sao chép: $text", Toast.LENGTH_SHORT).show()
                    // Có thể đóng activity này sau khi sao chép
                    finish()
                }
            }
            layout.addView(textView)
        }
    }
    
    // Hàm chuyển đổi dp sang pixel
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}

