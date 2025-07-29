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
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dung.clipboard.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var clipboard: ClipboardManager
    private var isServiceRunning = false

    private lateinit var binding: ActivityMainBinding

    // Biến tạm để lưu dữ liệu khi context menu được tạo
    private var selectedText: String? = null
    private var selectedIsPinned: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate: Activity created")
        ClipboardDataManager.initialize(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (!clipText.isNullOrBlank()) {
                Log.d("MainActivity", "Clip changed in MainActivity: $clipText")
                ClipboardDataManager.addCopy(clipText)
                addCopiedAndPinnedItems() // ĐÃ SỬA: Thay thế recreate() bằng addCopiedAndPinnedItems()
            }
        }

        binding.toggleServiceButton.setOnClickListener {
            if (isServiceRunning) {
                stopFloatingWidgetService()
                Toast.makeText(this@MainActivity, "Đã tắt Clipboard Nổi", Toast.LENGTH_SHORT).show() // Đã sửa hoàn chỉnh Toast
                Log.d("MainActivity", "Toggle service: Stopping service")
            } else {
                startFloatingWidgetService()
                Log.d("MainActivity", "Toggle service: Starting service")
            }
        }

        addCopiedAndPinnedItems()
        isServiceRunning = isMyServiceRunning(FloatingWidgetService::class.java)
        updateToggleButtonText()
    }
    // Các hàm còn lại của MainActivity (startFloatingWidgetService, stopFloatingWidgetService,
    // isMyServiceRunning, updateToggleButtonText, addCopiedAndPinnedItems, onCreateContextMenu,
    // onContextItemSelected, deleteItem, togglePinStatus) giữ nguyên như trước.
    // Bạn cần đảm bảo chúng đầy đủ trong file của mình.

    // Ví dụ: startFloatingWidgetService, stopFloatingWidgetService, isMyServiceRunning,
    // updateToggleButtonText, addCopiedAndPinnedItems, onCreateContextMenu, onContextItemSelected,
    // deleteItem, togglePinStatus
    // (Lưu ý: Bạn cần đảm bảo các hàm này có trong file MainActivity.kt của bạn.
    // Tôi không thể viết lại toàn bộ nếu không có mã gốc của các hàm đó.)
    private fun startFloatingWidgetService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Vui lòng cấp quyền hiển thị trên các ứng dụng khác", Toast.LENGTH_LONG).show()
        } else {
            val serviceIntent = Intent(this, FloatingWidgetService::class.java)
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

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun updateToggleButtonText() {
        binding.toggleServiceButton.text = if (isServiceRunning) "TẮT CLIPBOARD NỔI" else "BẬT CLIPBOARD NỔI"
    }

    private fun addCopiedAndPinnedItems() {
        binding.copiedItemsContainer.removeAllViews()
        binding.pinnedItemsContainer.removeAllViews()

        // Sắp xếp theo thời gian mới nhất lên đầu (tùy chọn)
        val sortedCopies = ClipboardDataManager.getAllCopies().sortedByDescending { it.timestamp }

        for (item in sortedCopies) {
            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8)
                }
                text = item.text
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.parseColor("#E0E0E0"))
                setTextColor(Color.BLACK)
                tag = item.text // Dùng tag để lưu text cho context menu
                setTextIsSelectable(true) // Cho phép chọn văn bản
                setOnLongClickListener {
                    selectedText = item.text
                    selectedIsPinned = item.isPinned
                    false // Trả về false để kích hoạt Context Menu
                }
            }
            registerForContextMenu(textView)

            if (item.isPinned) {
                binding.pinnedItemsContainer.addView(textView)
            } else {
                binding.copiedItemsContainer.addView(textView)
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle("Tùy chọn")
        menu?.add(0, 0, 0, "Sao chép")
        menu?.add(0, 1, 1, if (selectedIsPinned) "Bỏ ghim" else "Ghim")
        menu?.add(0, 2, 2, "Xóa")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> { // Sao chép
                selectedText?.let { text ->
                    val clip = android.content.ClipData.newPlainText("Copied Text", text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Đã sao chép: $text", Toast.LENGTH_SHORT).show()
                }
            }
            1 -> { // Ghim/Bỏ ghim
                selectedText?.let { text ->
                    togglePinStatus(text)
                }
            }
            2 -> { // Xóa
                selectedText?.let { text ->
                    deleteItem(text)
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun deleteItem(text: String) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa mục này không?")
            .setPositiveButton("Xóa") { dialog, which ->
                ClipboardDataManager.deleteCopy(text)
                addCopiedAndPinnedItems() // Cập nhật lại danh sách sau khi xóa
                Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun togglePinStatus(text: String) {
        ClipboardDataManager.togglePin(text)
        addCopiedAndPinnedItems() // Cập nhật lại danh sách sau khi ghim/bỏ ghim
        Toast.makeText(this, if (selectedIsPinned) "Đã bỏ ghim" else "Đã ghim", Toast.LENGTH_SHORT).show()
    }
}

