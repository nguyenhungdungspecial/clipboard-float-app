package com.dung.clipboard

import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ContextMenu
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dung.clipboard.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var clipboard: ClipboardManager
    private lateinit var binding: ActivityMainBinding
    private var selectedText: String? = null
    private var selectedIsPinned: Boolean = false
    private lateinit var fileLogger: FileLogger

    private val updateUIReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.dung.clipboard.ACTION_UPDATE_UI") {
                fileLogger.log("MainActivity", "Received broadcast to update UI. Forcing UI update.")
                updateUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Toast.makeText(this, "Toast 1: Bắt đầu onCreate", Toast.LENGTH_SHORT).show()

        fileLogger = FileLogger(this)
        fileLogger.log("MainActivity", "onCreate: Activity created")
        // Toast.makeText(this, "Toast 2: Đã khởi tạo FileLogger", Toast.LENGTH_SHORT).show()

        ClipboardDataManager.initialize(this)
        // Toast.makeText(this, "Toast 3: Đã khởi tạo ClipboardDataManager", Toast.LENGTH_SHORT).show()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Toast.makeText(this, "Toast 4: Đã thiết lập giao diện", Toast.LENGTH_SHORT).show()

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val filter = IntentFilter("com.dung.clipboard.ACTION_UPDATE_UI")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateUIReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(updateUIReceiver, filter)
        }

        try {
            binding.toggleServiceButton.setOnClickListener {
                fileLogger.log("MainActivity", "Toggle service button clicked.")
                if (isMyServiceRunning(FloatingWidgetService::class.java)) {
                    fileLogger.log("MainActivity", "Service is running, stopping it.")
                    stopFloatingWidgetService()
                    Toast.makeText(this, "Đã tắt Clipboard Nổi", Toast.LENGTH_SHORT).show()
                } else {
                    fileLogger.log("MainActivity", "Service is not running, starting it.")
                    startFloatingWidgetService()
                }
                updateToggleButtonText()
            }
            binding.clearAllButton.setOnClickListener {
                showConfirmClearDialog()
            }
            binding.viewLogButton.setOnClickListener {
                showLogDialog()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Lỗi khi gán listener cho các view: ${e.message}", e)
            Toast.makeText(this, "Lỗi: Không thể tìm thấy các nút bấm.", Toast.LENGTH_LONG).show()
        }

        updateUI()
        // Toast.makeText(this, "Toast 5: Kết thúc onCreate", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        fileLogger.log("MainActivity", "onResume: Activity resumed, forcing UI update.")
        updateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateUIReceiver)
        fileLogger.log("MainActivity", "onDestroy: Receiver unregistered.")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        fileLogger.log("MainActivity", "onNewIntent: Received new intent with action ${intent?.action}")
        if (intent?.action == "com.dung.clipboard.ACTION_TOGGLE_UI") {
            fileLogger.log("MainActivity", "Received toggle action, finishing activity.")
            finish()
        }
        updateUI()
    }

    private fun updateUI() {
        // Toast.makeText(this, "Toast 4.1: Bắt đầu updateUI", Toast.LENGTH_SHORT).show()
        fileLogger.log("MainActivity", "updateUI: Refreshing UI elements")
        addCopiedAndPinnedItems()
        updateToggleButtonText()
        // Toast.makeText(this, "Toast 4.2: Kết thúc updateUI", Toast.LENGTH_SHORT).show()
    }

    private fun addCopiedAndPinnedItems() {
        // Toast.makeText(this, "Toast 4.1.1: Bắt đầu addCopiedAndPinnedItems", Toast.LENGTH_SHORT).show()
        fileLogger.log("MainActivity", "addCopiedAndPinnedItems: Refreshing lists")
        binding.copiedLayout.removeViews(1, binding.copiedLayout.childCount - 1)
        binding.pinnedLayout.removeViews(1, binding.pinnedLayout.childCount - 1)

        val copiedList = ClipboardDataManager.getCopiedList().take(10)
        // Toast.makeText(this, "Toast 4.1.2: Lấy copiedList thành công, số lượng: ${copiedList.size}", Toast.LENGTH_SHORT).show()

        copiedList.forEachIndexed { index, text ->
            // Toast.makeText(this, "Toast 4.1.3: Đang xử lý item copied $index", Toast.LENGTH_SHORT).show()
            binding.copiedLayout.addView(createTextItem(text, false))
        }

        val pinnedList = ClipboardDataManager.getPinnedList()
        // Toast.makeText(this, "Toast 4.1.4: Lấy pinnedList thành công, số lượng: ${pinnedList.size}", Toast.LENGTH_SHORT).show()

        pinnedList.forEachIndexed { index, text ->
            // Toast.makeText(this, "Toast 4.1.5: Đang xử lý item pinned $index", Toast.LENGTH_SHORT).show()
            binding.pinnedLayout.addView(createTextItem(text, true))
        }

        // Toast.makeText(this, "Toast 4.1.6: Kết thúc addCopiedAndPinnedItems", Toast.LENGTH_SHORT).show()
    }

    private fun startFloatingWidgetService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 123)
            Toast.makeText(this, "Vui lòng cấp quyền vẽ đè lên ứng dụng khác", Toast.LENGTH_LONG).show()
        } else {
            val serviceIntent = Intent(this, FloatingWidgetService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "Đã bật Clipboard Nổi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopFloatingWidgetService() {
        val serviceIntent = Intent(this, FloatingWidgetService::class.java)
        stopService(serviceIntent)
    }

    private fun updateToggleButtonText() {
        if (isMyServiceRunning(FloatingWidgetService::class.java)) {
            binding.toggleServiceButton.text = "Tắt Clipboard Nổi"
            (binding.toggleServiceButton as? Button)?.setBackgroundColor(Color.RED)
        } else {
            binding.toggleServiceButton.text = "Bật Clipboard Nổi"
            (binding.toggleServiceButton as? Button)?.setBackgroundColor(Color.GREEN)
        }
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

    private fun createTextItem(text: String, isPinned: Boolean): LinearLayout {
        val container = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.item_background)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            this.text = text
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 16, 0)
            maxLines = 2
            setOnClickListener {
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Copied Text", text))
                Toast.makeText(this@MainActivity, "Đã sao chép: $text", Toast.LENGTH_SHORT).show()
            }
            setOnLongClickListener {
                selectedText = text
                selectedIsPinned = isPinned
                false
            }
        }
        registerForContextMenu(textView)

        val pinButton = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 8, 0)
            }
            setImageResource(if (isPinned) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
            setOnClickListener {
                if (isPinned) {
                    ClipboardDataManager.unpinText(text)
                } else {
                    ClipboardDataManager.pinText(text)
                }
                updateUI()
                Toast.makeText(this@MainActivity, if (isPinned) "Đã bỏ ghim" else "Đã ghim", Toast.LENGTH_SHORT).show()
            }
        }

        val deleteButton = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 0)
            }
            setImageResource(android.R.drawable.ic_delete)
            setOnClickListener {
                showConfirmDeleteDialog(text, isPinned)
            }
        }

        container.addView(textView)
        container.addView(pinButton)
        container.addView(deleteButton)

        return container
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
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
                    updateUI()
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
                updateUI()
                Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showConfirmClearDialog() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa tất cả")
            .setMessage("Bạn có chắc chắn muốn xóa tất cả các mục đã sao chép và đã ghim không?")
            .setPositiveButton("Xóa tất cả") { dialog, _ ->
                fileLogger.clearLogs()
                ClipboardDataManager.clearAllData()
                updateUI()
                Toast.makeText(this, "Đã xóa tất cả dữ liệu", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showLogDialog() {
        val logContent = fileLogger.getLogs()
        val logTextView = TextView(this).apply {
            text = logContent
            setPadding(30, 30, 30, 30)
            textSize = 12f
        }
        AlertDialog.Builder(this)
            .setTitle("Log của ứng dụng")
            .setView(logTextView)
            .setPositiveButton("Đóng", null)
            .setNegativeButton("Xóa Log") { _, _ ->
                fileLogger.clearLogs()
                Toast.makeText(this, "Đã xóa file log", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}

