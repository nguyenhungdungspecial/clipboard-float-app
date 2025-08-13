package com.dung.clipboard

import android.content.*
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

    // để FloatingWidgetService biết Activity đang mở hay đóng
    companion object { @JvmField var isVisible: Boolean = false }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_ITEMS_UPDATED -> refreshFromStore()
                ACTION_CLOSE_ACTIVITY -> finish()
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

        items.addAll(ClipboardDataManager.getItems(this))
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        lvCopied.adapter = adapter
        updateStatus()

        // đảm bảo service lắng nghe clipboard đang chạy
        val svc = Intent(this, ClipboardService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svc) else startService(svc)

        btnOpenAccessibility.setOnClickListener { openAccessibilitySettings() }
        btnToggleFloat.setOnClickListener { ensureOverlayPermission { toggleFloatingWidget() } }
        btnClear.setOnClickListener {
            ClipboardDataManager.clear(this)
        }
    }

    override fun onStart() {
        super.onStart()
        isVisible = true
        registerReceiver(receiver, IntentFilter().apply {
            addAction(ACTION_ITEMS_UPDATED)
            addAction(ACTION_CLOSE_ACTIVITY)
        })
    }

    override fun onStop() {
        isVisible = false
        runCatching { unregisterReceiver(receiver) }
        super.onStop()
    }

    private fun refreshFromStore() {
        items.clear()
        items.addAll(ClipboardDataManager.getItems(this))
        adapter.notifyDataSetChanged()
        updateStatus()
    }

    private fun updateStatus() {
        tvStatus.text = if (items.isEmpty()) "Clipboard rỗng" else "Có ${items.size} mục"
    }

    private fun openAccessibilitySettings() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Vào Trợ năng → bật \"Clipboard Float App\"", Toast.LENGTH_LONG).show()
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
