package com.dung.clipboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ClipboardActivity : AppCompatActivity() {

    private lateinit var lvCopied: ListView
    private lateinit var tvStatus: TextView
    private lateinit var btnOpenAccessibility: Button
    private lateinit var btnToggleFloat: Button
    private lateinit var btnClear: Button

    private lateinit var adapter: ArrayAdapter<String>
    private val items = mutableListOf<String>()

    // nhận tín hiệu cập nhật & đóng
    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ClipboardStorage.ACTION_ITEMS_UPDATED -> refreshFromStorage()
                ClipboardStorage.ACTION_CLOSE_ACTIVITY -> finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lvCopied = findViewById(R.id.lvCopied)
        tvStatus = findViewById(R.id.tvStatus)
        btnOpenAccessibility = findViewById(R.id.btnRequestAccessibility)
        btnToggleFloat = findViewById(R.id.btnToggleFloat)
        btnClear = findViewById(R.id.btnClear)

        items.addAll(ClipboardStorage.getItems(this))
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        lvCopied.adapter = adapter
        updateStatus()

        // đảm bảo service clipboard chạy
        val svc = Intent(this, ClipboardService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svc) else startService(svc)

        btnOpenAccessibility.setOnClickListener { openAccessibilitySettings() }
        btnToggleFloat.setOnClickListener { ensureOverlayPermission { toggleFloatingWidget() } }
        btnClear.setOnClickListener {
            items.clear()
            ClipboardStorage.saveItems(this, items)
            adapter.notifyDataSetChanged(); updateStatus()
        }
    }

    override fun onStart() {
        super.onStart()
        ActivityVisibility.visible = true
        registerReceiver(updateReceiver, IntentFilter().apply {
            addAction(ClipboardStorage.ACTION_ITEMS_UPDATED)
            addAction(ClipboardStorage.ACTION_CLOSE_ACTIVITY)
        })
    }

    override fun onStop() {
        ActivityVisibility.visible = false
        runCatching { unregisterReceiver(updateReceiver) }
        super.onStop()
    }

    private fun refreshFromStorage() {
        items.clear()
        items.addAll(ClipboardStorage.getItems(this))
        adapter.notifyDataSetChanged()
        updateStatus()
    }

    private fun updateStatus() {
        tvStatus.text = if (items.isEmpty()) "Clipboard rỗng" else "Có ${items.size} mục"
    }

    private fun openAccessibilitySettings() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Tìm \"Clipboard Float App\" và bật Trợ năng", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Không mở được Cài đặt Trợ năng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ensureOverlayPermission(onGranted: () -> Unit) {
        if (Settings.canDrawOverlays(this)) { onGranted(); return }
        Toast.makeText(this, "Cần quyền Hiển thị trên ứng dụng khác", Toast.LENGTH_LONG).show()
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
    }

    private fun toggleFloatingWidget() {
        if (FloatingWidgetService.isRunning) {
            stopService(Intent(this, FloatingWidgetService::class.java))
            Toast.makeText(this, "Đã tắt icon nổi", Toast.LENGTH_SHORT).show()
        } else {
            startService(Intent(this, FloatingWidgetService::class.java))
            Toast.makeText(this, "Đã bật icon nổi", Toast.LENGTH_SHORT).show()
        }
    }
}
