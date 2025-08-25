package com.dung.clipboard

import android.content.*
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.os.Build

class MainActivity : AppCompatActivity() {

    companion object {
        var isVisible = false
        const val ACTION_CLIPBOARD_UPDATED = "com.dung.clipboard.CLIPBOARD_UPDATED"
        const val ACTION_TOGGLE_FINISH = "com.dung.clipboard.TOGGLE_FINISH"
    }

    private lateinit var tvStatus: TextView
    private lateinit var lvCopied: ListView
    private lateinit var lvPinned: ListView
    private lateinit var btnRequestAccessibility: Button
    private lateinit var btnToggleFloat: Button
    private lateinit var btnClear: Button

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_CLIPBOARD_UPDATED -> refreshList()
                ACTION_TOGGLE_FINISH -> finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        lvCopied = findViewById(R.id.lvCopied)
        lvPinned = findViewById(R.id.lvPinned)

        // Khôi phục ánh xạ đúng với layout gốc của bạn
        btnRequestAccessibility = findViewById(R.id.btnRequestAccessibility)
        btnToggleFloat = findViewById(R.id.btnToggleFloat)
        btnClear = findViewById(R.id.btnClear)

        // Cài đặt text cho các nút
        btnRequestAccessibility.text = "MỞ CÀI ĐẶT TRỢ NĂNG"
        btnToggleFloat.text = "BẬT/TẮT ICON NỔI"
        btnClear.text = "XÓA LỊCH SỬ"

        btnRequestAccessibility.setOnClickListener { openAccessibilitySettings() }
        btnToggleFloat.setOnClickListener { toggleFloatingWidget() }
        btnClear.setOnClickListener { clearClipboardHistory() }

        // Start services
        startService(Intent(this, ClipboardService::class.java))
        startService(Intent(this, FloatingWidgetService::class.java))

        registerReceiver(receiver, IntentFilter(ACTION_CLIPBOARD_UPDATED))
        registerReceiver(receiver, IntentFilter(ACTION_TOGGLE_FINISH))

        updateAccessibilityStatus()
        refreshList()
    }

    override fun onResume() {
        super.onResume()
        isVisible = true
        refreshList()
        updateAccessibilityStatus()
        requestOverlayPermission() // Thêm lời gọi này
    }

    override fun onPause() {
        super.onPause()
        isVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(receiver) } catch (e: Exception) {}
    }

    private fun refreshList() {
        val items = ClipboardDataManager.getCopiedList(this)
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        lvCopied.adapter = adapter
        val pinned = ClipboardDataManager.getPinnedList(this)
        val padapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, pinned)
        lvPinned.adapter = padapter

        tvStatus.text = if (items.isEmpty()) "Clipboard rỗng" else "Có ${items.size} mục"
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    // Thêm hàm này để yêu cầu quyền vẽ lên các ứng dụng khác
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 1234)
        }
    }

    private fun updateAccessibilityStatus() {
        tvStatus.text = if (Utils.isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
            "Accessibility đã bật"
        } else {
            "Accessibility chưa bật"
        }
    }

    private fun toggleFloatingWidget() {
        if (FloatingWidgetService.isRunning) {
            stopService(Intent(this, FloatingWidgetService::class.java))
            btnToggleFloat.text = "BẬT/TẮT ICON NỔI"
        } else {
            startService(Intent(this, FloatingWidgetService::class.java))
            btnToggleFloat.text = "BẬT/TẮT ICON NỔI"
        }
    }

    private fun clearClipboardHistory() {
        ClipboardDataManager.clearAll(this)
        refreshList()
    }
}
