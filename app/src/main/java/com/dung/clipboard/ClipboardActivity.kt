
package com.dung.clipboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ClipboardActivity : AppCompatActivity() {

    private lateinit var lvCopied: ListView
    private lateinit var tvStatus: TextView
    private lateinit var btnOpenAccessibility: Button
    private lateinit var btnToggleFloat: Button
    private lateinit var btnClear: Button

    private val items = mutableListOf<String>()
    private val prefs by lazy {
        getSharedPreferences("clipboard_data", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lvCopied = findViewById(R.id.lvCopied)
        tvStatus = findViewById(R.id.tvStatus)
        btnOpenAccessibility = findViewById(R.id.btnRequestAccessibility)
        btnToggleFloat = findViewById(R.id.btnToggleFloat)
        btnClear = findViewById(R.id.btnClear)

        // List hiển thị lịch sử
        loadItems()
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        lvCopied.adapter = adapter
        updateStatus()

        // Nhận clip mới từ ClipboardService
        intent.getStringExtra("new_clip")?.let { addClipboardItem(it, adapter) }

        // Mở trang cài đặt Trợ năng, nơi có service của app
        btnOpenAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }

        // Bật/tắt icon ngôi sao nổi
        btnToggleFloat.setOnClickListener {
            ensureOverlayPermission {
                toggleFloatingWidget()
            }
        }

        // Xóa lịch sử
        btnClear.setOnClickListener {
            items.clear()
            saveItems()
            adapter.notifyDataSetChanged()
            updateStatus()
        }

        // Đảm bảo service clipboard đang chạy để lắng nghe dữ liệu mới
        startClipboardServiceIfNeeded()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val adapter = lvCopied.adapter as android.widget.ArrayAdapter<String>
        intent?.getStringExtra("new_clip")?.let { addClipboardItem(it, adapter) }
    }

    private fun startClipboardServiceIfNeeded() {
        val svc = Intent(this, ClipboardService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svc)
        } else {
            startService(svc)
        }
    }

    private fun openAccessibilitySettings() {
        try {
            // Mở trang cài đặt Trợ năng chung (ổn định trên mọi máy)
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Tìm \"Clipboard Float App\" và bật Trợ năng", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Không mở được Cài đặt Trợ năng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ensureOverlayPermission(onGranted: () -> Unit) {
        if (Settings.canDrawOverlays(this)) {
            onGranted()
            return
        }
        Toast.makeText(this, "Cần cấp quyền \"Hiển thị trên các ứng dụng khác\"", Toast.LENGTH_LONG).show()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun toggleFloatingWidget() {
        if (FloatingWidgetService.isRunning) {
            stopService(Intent(this, FloatingWidgetService::class.java))
            Toast.makeText(this, "Đã tắt icon nổi", Toast.LENGTH_SHORT).show()
        } else {
            val i = Intent(this, FloatingWidgetService::class.java)
            startService(i)
            Toast.makeText(this, "Đã bật icon nổi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addClipboardItem(text: String, adapter: android.widget.ArrayAdapter<String>) {
        if (text.isBlank()) return
        if (items.isNotEmpty() && items[0] == text) return
        items.add(0, text)
        if (items.size > 200) items.removeLast()
        saveItems()
        adapter.notifyDataSetChanged()
        updateStatus()
    }

    private fun loadItems() {
        val set = prefs.getStringSet("items", emptySet()) ?: emptySet()
        items.clear()
        items.addAll(set.toList().reversed())
    }

    private fun saveItems() {
        prefs.edit().putStringSet("items", items.reversed().toSet()).apply()
    }

    private fun updateStatus() {
        tvStatus.text = if (items.isEmpty()) "Clipboard rỗng" else "Có ${items.size} mục"
    }
}

