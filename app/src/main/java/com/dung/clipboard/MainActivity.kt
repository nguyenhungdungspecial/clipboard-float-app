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
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
    private var isServiceRunning = false
    private lateinit var binding: ActivityMainBinding
    private var selectedText: String? = null
    private var selectedIsPinned: Boolean = false

    private val clipboardUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.dung.clipboard.ACTION_CLIPBOARD_UPDATE") {
                Log.d("MainActivity", "Broadcast received, refreshing UI.")
                updateUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate: Activity created")
        ClipboardDataManager.initialize(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        binding.toggleServiceButton.setOnClickListener {
            if (isServiceRunning) {
                stopFloatingWidgetService()
                Toast.makeText(this, "Đã tắt Clipboard Nổi", Toast.LENGTH_SHORT).show()
                Log.d("MainActivity", "Toggle service: Stopping service")
            } else {
                startFloatingWidgetService()
                Log.d("MainActivity", "Toggle service: Starting service")
            }
        }

        updateUI()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume: Activity resumed")
        val filter = IntentFilter("com.dung.clipboard.ACTION_CLIPBOARD_UPDATE")
        registerReceiver(clipboardUpdateReceiver, filter)
        updateUI()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause: Activity paused")
        unregisterReceiver(clipboardUpdateReceiver)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent: Received new intent with action ${intent?.action}")
        // Xử lý logic đóng/mở giao diện khi nhấn icon nổi
        if (intent?.action == "com.dung.clipboard.ACTION_TOGGLE_UI") {
            Log.d("MainActivity", "Received toggle action, finishing activity.")
            finish()
        }
    }

    private fun updateUI() {
        Log.d("MainActivity", "updateUI: Refreshing UI elements")
        addCopiedAndPinnedItems()
        isServiceRunning = isMyServiceRunning(FloatingWidgetService::class.java)
        updateToggleButtonText()
    }

    private fun addCopiedAndPinnedItems() {
        Log.d("MainActivity", "addCopiedAndPinnedItems: Refreshing lists")
        if (binding.copiedLayout.childCount > 1) {
            binding.copiedLayout.removeViews(1, binding.copiedLayout.childCount - 1)
        }
        if (binding.pinnedLayout.childCount > 1) {
            binding.pinnedLayout.removeViews(1, binding.pinnedLayout.childCount - 1)
        }

        ClipboardDataManager.getCopiedList().forEach { text ->
            binding.copiedLayout.addView(createTextItem(text, false))
            Log.d("MainActivity", "Added copied item: $text")
        }

        ClipboardDataManager.getPinnedList().forEach { text ->
            binding.pinnedLayout.addView(createTextItem(text, true))
            Log.d("MainActivity", "Added pinned item: $text")
        }
    }

    private fun startFloatingWidgetService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))
            startActivityForResult(intent, 123)
            Toast.makeText(this, "Vui lòng cấp quyền vẽ đè lên ứng dụng khác", Toast.LENGTH_LONG).show()
        } else {
            val serviceIntent = Intent(this, FloatingWidgetService::class.java)
            startService(serviceIntent)
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
        if (isServiceRunning) {
            binding.toggleServiceButton.text = "Tắt Clipboard Nổi"
            binding.toggleServiceButton.setBackgroundColor(Color.RED)
        } else {
            binding.toggleServiceButton.text = "Bật Clipboard Nổi"
            binding.toggleServiceButton.setBackgroundColor(Color.GREEN)
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
                setMargins(8, 0, 0, 0)
            }
            setImageResource(if (isPinned) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
            setOnClickListener {
                if (isPinned) {
                    ClipboardDataManager.unpinText(text)
                    Toast.makeText(this@MainActivity, "Đã bỏ ghim", Toast.LENGTH_SHORT).show()
                } else {
                    ClipboardDataManager.pinText(text)
                    Toast.makeText(this@MainActivity, "Đã ghim", Toast.LENGTH_SHORT).show()
                }
                updateUI()
            }
        }

        val deleteButton = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 0, 0)
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
}
