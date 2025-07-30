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
// ĐÃ XÓA: import com.dung.clipboard.databinding.ActivityMainBinding vì không dùng View Binding

class MainActivity : AppCompatActivity() {

    private lateinit var clipboard: ClipboardManager
    private var isServiceRunning = false

    // Biến để giữ tham chiếu đến cột "Đã copy" và "Đã ghim"
    private lateinit var copiedLayout: LinearLayout
    private lateinit var pinnedLayout: LinearLayout

    // Biến tạm để lưu dữ liệu khi context menu được tạo
    private var selectedText: String? = null
    private var selectedIsPinned: Boolean = false

    // Biến để giữ tham chiếu đến nút bật/tắt dịch vụ (vì không dùng View Binding)
    private lateinit var toggleServiceButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate: Activity created")
        ClipboardDataManager.initialize(this)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (!clipText.isNullOrBlank()) {
                Log.d("MainActivity", "Clip changed in MainActivity: $clipText")
                ClipboardDataManager.addCopy(clipText)
                updateUI() // ĐÃ SỬA: Gọi hàm updateUI() thay vì recreate()
            }
        }

        // Layout chính (ngang)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL // Đã sửa thành VERTICAL để chứa nút toggle
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // --- Nút bật/tắt dịch vụ Floating Widget ---
        toggleServiceButton = Button(this).apply {
            text = "Bật/Tắt Clipboard Nổi"
            setOnClickListener {
                if (isServiceRunning) {
                    stopFloatingWidgetService()
                    Toast.makeText(this@MainActivity, "Đã tắt Clipboard Nổi", Toast.LENGTH_SHORT).show()
                    Log.d("MainActivity", "Toggle service: Stopping service")
                } else {
                    startFloatingWidgetService()
                    Log.d("MainActivity", "Toggle service: Starting service")
                }
            }
        }
        mainLayout.addView(toggleServiceButton) // Thêm nút vào mainLayout

        // Layout chứa 2 cột (ngang)
        val contentColumnsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT // Chiếm phần còn lại của màn hình
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

        contentColumnsLayout.addView(copiedLayout)
        contentColumnsLayout.addView(divider)
        contentColumnsLayout.addView(pinnedLayout)

        mainLayout.addView(contentColumnsLayout) // Thêm layout cột vào mainLayout

        setContentView(mainLayout)

        // Cập nhật giao diện lần đầu khi Activity được tạo
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume: Activity resumed, refreshing items")
        ClipboardDataManager.initialize(this) // Đảm bảo dữ liệu được tải mới nhất
        updateUI() // ĐÃ SỬA: Gọi updateUI()
    }

    // Hàm tạo Container cho cột (bao gồm tiêu đề và danh sách item)
    private fun createColumnContainer(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        }
    }

    // Hàm tạo tiêu đề cột
    private fun createColumnTitle(title: String, isPinned: Boolean): TextView {
        return TextView(this).apply {
            text = title
            textSize = 18f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(if (isPinned) 0xFFB2DFDB.toInt() else 0xFFB3E5FC.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    // Hàm cập nhật lại toàn bộ giao diện
    private fun updateUI() {
        Log.d("MainActivity", "updateUI: Refreshing lists")
        // Xóa tất cả các view cũ trong cả hai cột
        copiedLayout.removeAllViews()
        pinnedLayout.removeAllViews()

        // Thêm lại tiêu đề cho từng cột
        copiedLayout.addView(createColumnTitle("Đã copy", false))
        pinnedLayout.addView(createColumnTitle("Đã ghim", true))

        // Thêm lại các mục đã copy và đã ghim vào các cột tương ứng
        ClipboardDataManager.getCopiedList().forEach { text ->
            copiedLayout.addView(createTextItem(text, false))
            Log.d("MainActivity", "Added copied item: $text")
        }
        ClipboardDataManager.getPinnedList().forEach { text ->
            pinnedLayout.addView(createTextItem(text, true))
            Log.d("MainActivity", "Added pinned item: $text")
        }

        // Cập nhật trạng thái nút bật/tắt dịch vụ
        isServiceRunning = isMyServiceRunning(FloatingWidgetService::class.java)
        updateToggleButtonText()
    }

    private fun createTextItem(text: String, isPinned: Boolean): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(8, 8, 8, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8) // Margin giữa các item
            }
            setBackgroundResource(R.drawable.item_background) // Giả sử bạn có item_background.xml
        }

        val textView = TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                val clip = android.content.ClipData.newPlainText("Copied Text", text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@MainActivity, "Đã sao chép: $text", Toast.LENGTH_SHORT).show()
            }
            setOnLongClickListener {
                selectedText = text
                selectedIsPinned = isPinned
                false // Trả về false để hệ thống tạo Context Menu
            }
        }
        registerForContextMenu(textView)

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
                            Toast.makeText(this@MainActivity, "Đã lưu chỉnh sửa", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Nội dung không được để trống", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@MainActivity, if (isPinned) "Đã bỏ ghim" else "Đã ghim", Toast.LENGTH_SHORT).show()
            }
        }

        val deleteBtn = Button(this).apply {
            text = "Xoá"
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                  .setTitle("Xác nhận xóa")
                  .setMessage("Bạn có chắc chắn muốn xóa mục này không?\n\"$text\"")
                  .setPositiveButton("Xóa") { _, _ ->
                        ClipboardDataManager.removeText(text, isPinned)
                        updateUI() // ĐÃ SỬA: Gọi hàm updateUI()
                        Toast.makeText(this@MainActivity, "Đã xóa", Toast.LENGTH_SHORT).show()
                    }
                  .setNegativeButton("Hủy", null)
                  .show()
            }
        }

        container.addView(textView)
        container.addView(editBtn)
        container.addView(pinBtn)
        container.addView(deleteBtn)

        return container
    }

    // =========================================================================================
    // Context Menu và Dialogs
    // =========================================================================================

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu) // Giả sử bạn có R.menu.context_menu
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_edit -> {
                selectedText?.let { text ->
                    showEditDialog(text, selectedIsPinned)
                }
                true
            }
            R.id.menu_delete -> {
                selectedText?.let { text ->
                    showConfirmDeleteDialog(text, selectedIsPinned)
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun showEditDialog(oldText: String, isPinned: Boolean) {
        val input = EditText(this)
        input.setText(oldText)
        AlertDialog.Builder(this)
          .setTitle("Chỉnh sửa nội dung")
          .setView(input)
          .setPositiveButton("Lưu") { dialog, _ ->
                val newText = input.text.toString()
                if (newText.isNotBlank()) {
                    ClipboardDataManager.editText(oldText, newText, isPinned)
                    updateUI() // ĐÃ SỬA: Gọi hàm updateUI()
                    Toast.makeText(this, "Đã lưu chỉnh sửa", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
          .setNegativeButton("Hủy") { dialog, _ ->
                dialog.cancel()
            }
          .show()
    }

    private fun showConfirmDeleteDialog(text: String, isPinned: Boolean) {
        AlertDialog.Builder(this)
          .setTitle("Xác nhận xóa")
          .setMessage("Bạn có chắc chắn muốn xóa mục này không?\n\"$text\"")
          .setPositiveButton("Xóa") { dialog, _ ->
                ClipboardDataManager.removeText(text, isPinned)
                updateUI() // ĐÃ SỬA: Gọi hàm updateUI()
                Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
          .setNegativeButton("Hủy") { dialog, _ ->
                dialog.cancel()
            }
          .show()
    }

    // =========================================================================================
    // Các hàm liên quan đến dịch vụ nổi (đã được thêm vào từ các phiên bản trước)
    // =========================================================================================
    private fun startFloatingWidgetService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))
            startActivityForResult(intent, 123)
            Toast.makeText(this, "Vui lòng cấp quyền vẽ đè lên ứng dụng khác", Toast.LENGTH_LONG).show()
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
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}

