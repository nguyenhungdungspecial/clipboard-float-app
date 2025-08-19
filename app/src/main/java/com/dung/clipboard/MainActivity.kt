package com.dung.clipboard

import android.content.*
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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
        btnRequestAccessibility = findViewById(R.id.btnRequestAccessibility)
        btnToggleFloat = findViewById(R.id.btnToggleFloat)
        btnClear = findViewById(R.id.btnClear)

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
            btnToggleFloat.text = "Bật icon nổi"
        } else {
            startService(Intent(this, FloatingWidgetService::class.java))
            btnToggleFloat.text = "Tắt icon nổi"
        }
    }

    private fun clearClipboardHistory() {
        ClipboardDataManager.clearAll(this)
        refreshList()
    }
}

