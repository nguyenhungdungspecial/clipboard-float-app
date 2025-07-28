package com.dung.clipboard

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var clipboard: ClipboardManager
    private var isServiceRunning = false // Biến để theo dõi trạng thái dịch vụ

    // Khai báo nút là thuộc tính của lớp
    private lateinit var toggleServiceButton: Button
    private lateinit var mainLayout: LinearLayout // Cũng khai báo mainLayout là thuộc tính để dễ truy cập

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (!clipText.isNullOrBlank()) {
                ClipboardDataManager.addCopy(clipText)
                recreate() // Tải lại Activity để cập nhật danh sách
            }
        }

        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // --- Nút bật/tắt dịch vụ Floating Widget ---
        toggleServiceButton = Button(this).apply {
            // Không cần findViewById vì đang tạo bằng code và gán trực tiếp
            text = "Bật/Tắt Clipboard Nổi"
            setOnClickListener {
                if (isServiceRunning) {
                    stopFloatingWidgetService()
                    Toast.makeText(this@MainActivity, "Đã tắt Clipboard Nổi", Toast.LENGTH_SHORT).show()
                } else {
                    startFloatingWidgetService()
                }
            }
        }
        mainLayout.addView(toggleServiceButton)
        // --- Kết thúc nút bật/tắt dịch vụ ---

        // Layout ngang với 2 cột (phần hiện tại của bạn)
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f // Cho phép nó chiếm phần còn lại của màn hình
            )
        }

        val copiedLayout = createColumn("Đã copy", ClipboardDataManager.getCopiedList(), false)
        val pinnedLayout = createColumn("Đã ghim", ClipboardDataManager.getPinnedList(), true)

        // Đường chia giữa
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                setMargins(8, 0, 8, 0)
            }
            setBackgroundColor(Color.DKGRAY)
        }

        contentLayout.addView(copiedLayout)
        contentLayout.addView(divider)
        contentLayout.addView(pinnedLayout)

        mainLayout.addView(contentLayout) // Thêm contentLayout vào mainLayout

        setContentView(mainLayout)

        // Kiểm tra trạng thái dịch vụ khi Activity được tạo
        isServiceRunning = isMyServiceRunning(FloatingWidgetService::class.java)
        updateToggleButtonText() // Cập nhật text ban đầu cho nút
    }

    private fun startFloatingWidgetService() {
        // Kiểm tra quyền SYSTEM_ALERT_WINDOW trước khi khởi động dịch vụ
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + packageName))
            startActivity(intent)
            Toast.makeText(this, "Vui lòng cấp quyền vẽ đè để bật tính năng nổi", Toast.LENGTH_LONG).show()
        } else {
            val serviceIntent = Intent(this, FloatingWidgetService::class.java)
            // Dùng startForegroundService cho Android O (API 26) trở lên
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            isServiceRunning = true
            updateToggleButtonText()
            Toast.makeText(this, "Đã bật Clipboard Nổi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopFloatingWidgetService() {
        val serviceIntent = Intent(this, FloatingWidgetService::class.java)
        stopService(serviceIntent)
        isServiceRunning = false
        updateToggleButtonText()
    }

    // Đã sửa hàm này để truy cập trực tiếp biến toggleServiceButton của lớp
    private fun updateToggleButtonText() {
        toggleServiceButton.text = if (isServiceRunning) "Tắt Clipboard Nổi" else "Bật Clipboard Nổi"
    }

    // Hàm kiểm tra xem một dịch vụ có đang chạy hay không
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    // Các hàm tạo cột và mục văn bản (không thay đổi)
    private fun createColumn(title: String, items: List<String>, isPinned: Boolean): LinearLayout {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 18f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(if (isPinned) 0xFFB2DFDB.toInt() else 0xFFB3E5FC.toInt())
        }
        column.addView(titleView)

        items.forEach { text ->
            column.addView(createTextItem(text, isPinned))
        }

        return column
    }

    private fun createTextItem(text: String, isPinned: Boolean): LinearLayout {
        val row = LinearLayout(this).apply {
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

        row.apply {
            addView(textView)
            addView(editBtn)
            addView(pinBtn)
            addView(deleteBtn)
        }

        return row
    }
}

