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
import com.dung.clipboard.databinding.ActivityMainBinding // Import View Binding Class

class MainActivity : AppCompatActivity() {

    private lateinit var clipboard: ClipboardManager
    private var isServiceRunning = false // Biến để theo dõi trạng thái dịch vụ

    // Khai báo View Binding object
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // Đặt layout gốc từ binding

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (!clipText.isNullOrBlank()) {
                ClipboardDataManager.addCopy(clipText)
                recreate() // Tải lại Activity để cập nhật danh sách
            }
        }

        // --- Nút bật/tắt dịch vụ Floating Widget ---
        binding.toggleServiceButton.setOnClickListener {
            if (isServiceRunning) {
                stopFloatingWidgetService()
                Toast.makeText(this@MainActivity, "Đã tắt Clipboard Nổi", Toast.LENGTH_SHORT).show()
            } else {
                startFloatingWidgetService()
            }
        }
        // --- Kết thúc nút bật/tắt dịch vụ ---

        // Thêm các item đã copy và đã ghim vào layout XML
        addCopiedAndPinnedItems()

        // Kiểm tra trạng thái dịch vụ khi Activity được tạo
        isServiceRunning = isMyServiceRunning(FloatingWidgetService::class.java)
        updateToggleButtonText() // Cập nhật text ban đầu cho nút
    }

    // Hàm mới để thêm các item vào layout
    private fun addCopiedAndPinnedItems() {
        // Xóa tất cả View cũ trong các cột trước khi thêm lại (để tránh trùng lặp khi recreate())
        // Giữ lại TextView tiêu đề, chỉ xóa các TextView nội dung
        if (binding.copiedLayout.childCount > 1) { // Kiểm tra nếu có hơn 1 con (1 là tiêu đề)
            binding.copiedLayout.removeViews(1, binding.copiedLayout.childCount - 1)
        }
        if (binding.pinnedLayout.childCount > 1) { // Kiểm tra nếu có hơn 1 con (1 là tiêu đề)
            binding.pinnedLayout.removeViews(1, binding.pinnedLayout.childCount - 1)
        }

        ClipboardDataManager.getCopiedList().forEach { text ->
            binding.copiedLayout.addView(createTextItem(text, false))
        }

        ClipboardDataManager.getPinnedList().forEach { text ->
            binding.pinnedLayout.addView(createTextItem(text, true))
        }
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

    private fun updateToggleButtonText() {
        binding.toggleServiceButton.text = if (isServiceRunning) "Tắt Clipboard Nổi" else "Bật Clipboard Nổi"
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

    // Hàm createColumn không còn cần thiết vì layout đã ở trong XML
    // Thay vào đó, chúng ta sẽ tạo riêng từng item và thêm vào LinearLayout đã có sẵn
    private fun createTextItem(text: String, isPinned: Boolean): LinearLayout {
        // Tạo LinearLayout cho mỗi item
        
var row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(8, 8, 8, 8)

        val textView = TextView(this)
        textView.text = text
        textView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        textView.setOnClickListener {
            val result = Intent().apply {
                putExtra("pasted_text", text)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
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

        row.addView(textView)
        row.addView(editBtn)
        row.addView(pinBtn)
        row.addView(deleteBtn)

        return row
    }
}

