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

    private lateinit var clipboard: ClipboardManager
    private lateinit var copiedLayout: LinearLayout // Biến để giữ tham chiếu đến cột "Đã copy"
    private lateinit var pinnedLayout: LinearLayout // Biến để giữ tham chiếu đến cột "Đã ghim"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo ClipboardDataManager. Đảm bảo ClipboardDataManager.kt cũng tồn tại và có các hàm cần thiết.
        ClipboardDataManager.initialize(this)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (!clipText.isNullOrBlank()) {
                ClipboardDataManager.addCopy(clipText)
                updateUI() // ĐÃ SỬA: Gọi hàm updateUI() thay vì recreate()
            }
        }

        // Layout chính (ngang)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Tạo các cột và gán vào biến thành viên
        copiedLayout = createColumnContainer()
        pinnedLayout = createColumnContainer()

        // Đường chia giữa
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                setMargins(8, 0, 8, 0)
            }
            setBackgroundColor(Color.DKGRAY)
        }

        mainLayout.addView(copiedLayout)
        mainLayout.addView(divider)
        mainLayout.addView(pinnedLayout)

        setContentView(mainLayout)

        // Cập nhật giao diện lần đầu khi Activity được tạo
        updateUI()
    }

    // Hàm tạo Container cho cột (bao gồm tiêu đề và danh sách item)
    private fun createColumnContainer(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        }
    }

    // Hàm tạo tiêu đề cột - ĐÃ SỬA LỖI "Val cannot be reassigned" bằng cách tường minh hơn
    private fun createColumnTitle(title: String, isPinned: Boolean): TextView {
        val textView = TextView(this) // Khởi tạo TextView

        // Tạo LayoutParams riêng biệt
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Gán LayoutParams cho TextView
        textView.layoutParams = params

        // Cấu hình các thuộc tính khác cho TextView
        textView.apply {
            text = title
            textSize = 18f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(if (isPinned) 0xFFB2DFDB.toInt() else 0xFFB3E5FC.toInt())
        }
        return textView
    }

    // Hàm cập nhật lại toàn bộ giao diện
    private fun updateUI() {
        // Xóa tất cả các view cũ trong cả hai cột
        copiedLayout.removeAllViews()
        pinnedLayout.removeAllViews()

        // Thêm lại tiêu đề cho từng cột
        copiedLayout.addView(createColumnTitle("Đã copy", false))
        pinnedLayout.addView(createColumnTitle("Đã ghim", true))

        // Thêm lại các mục đã copy và đã ghim vào các cột tương ứng
        // SỬ DỤNG getCopiedList() VÀ getPinnedList() TRỰC TIẾP
        ClipboardDataManager.getCopiedList().forEach { text ->
            copiedLayout.addView(createTextItem(text, false))
        }
        ClipboardDataManager.getPinnedList().forEach { text ->
            pinnedLayout.addView(createTextItem(text, true))
        }
    }

    private fun createTextItem(text: String, isPinned: Boolean): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(8, 8, 8, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
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
                val editText = EditText(this@MainActivity).apply {
                    setText(text)
                }
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Sửa nội dung")
                    .setView(editText)
                    .setPositiveButton("Lưu") { _, _ ->
                        val newText = editText.text.toString()
                        if (newText.isNotBlank()) {
                            ClipboardDataManager.editText(text, newText, isPinned)
                            updateUI() // ĐÃ SỬA: Gọi hàm updateUI()
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
                updateUI() // ĐÃ SỬA: Gọi hàm updateUI()
            }
        }

        val deleteBtn = Button(this).apply {
            text = "Xoá"
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa mục này không?")
                    .setPositiveButton("Xóa") { _, _ ->
                        ClipboardDataManager.removeText(text, isPinned)
                        updateUI() // ĐÃ SỬA: Gọi hàm updateUI()
                        Toast.makeText(this@MainActivity, "Đã xóa", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        }

        row.apply {
            addView(textView)
            addView(editBtn)
            addView(pinBtn)
            addView(deleteBtn)
        }

        return row
    }
}

