package com.dung.clipboard

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var lvCopied: ListView
    private lateinit var lvPinned: ListView
    private lateinit var tvStatus: TextView
    private lateinit var btnRequestAccessibility: Button
    private lateinit var btnToggleFloat: Button
    private lateinit var btnClear: Button

    private lateinit var copiedAdapter: ArrayAdapter<String>
    private lateinit var pinnedAdapter: ArrayAdapter<String>
    private val copied = mutableListOf<String>()
    private val pinned = mutableListOf<String>()

    private val ID_COPY = 1
    private val ID_PIN = 2
    private val ID_UNPIN = 3
    private val ID_DELETE = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // layout activity_main.xml của bạn — giữ nguyên
        setContentView(R.layout.activity_main)

        // map view theo ID quen dùng
        lvCopied = findViewById(R.id.lvCopied)
        lvPinned = findViewById(R.id.lvPinned)
        tvStatus = findViewById(R.id.tvStatus)
        btnRequestAccessibility = findViewById(R.id.btnRequestAccessibility)
        btnToggleFloat = findViewById(R.id.btnToggleFloat)
        btnClear = findViewById(R.id.btnClear)

        // adapters
        copiedAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, copied)
        pinnedAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, pinned)
        lvCopied.adapter = copiedAdapter
        lvPinned.adapter = pinnedAdapter

        // set EmptyView bằng code để tránh lỗi android:emptyView trong XML
        attachEmptyView(lvCopied, R.id.tvEmptyCopied)
        attachEmptyView(lvPinned, R.id.tvEmptyPinned)

        // load dữ liệu đã lưu
        refreshLists()

        // status dòng trên cùng
        renderStatus()

        // xin Accessibility
        btnRequestAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }

        // bật/tắt Floating Widget (Overlay permission)
        btnToggleFloat.setOnClickListener {
            if (!hasOverlayPermission()) {
                requestOverlayPermission()
            } else {
                toggleFloating()
            }
        }

        // clear history (chỉ clear Copied)
        btnClear.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Xoá lịch sử")
                .setMessage("Xoá toàn bộ danh sách Copied?")
                .setPositiveButton("Xoá") { _, _ ->
                    saveList("copied", emptyList())
                    refreshLists()
                }
                .setNegativeButton("Huỷ", null)
                .show()
        }

        // click item -> copy
        lvCopied.setOnItemClickListener { _, _, pos, _ ->
            safeCopyToClipboard(copied[pos])
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
        }
        lvPinned.setOnItemClickListener { _, _, pos, _ ->
            safeCopyToClipboard(pinned[pos])
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
        }

        // long click -> popup action (Copy / Pin / Unpin / Delete)
        lvCopied.setOnItemLongClickListener { v, _, pos, _ ->
            showItemMenu(v, source = "copied", position = pos)
            true
        }
        lvPinned.setOnItemLongClickListener { v, _, pos, _ ->
            showItemMenu(v, source = "pinned", position = pos)
            true
        }
    }

    // ====== Actions & helpers ======

    private fun showItemMenu(anchor: View, source: String, position: Int) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(Menu.NONE, ID_COPY, 0, "Copy")
        if (source == "copied") {
            popup.menu.add(Menu.NONE, ID_PIN, 1, "Pin")
        } else {
            popup.menu.add(Menu.NONE, ID_UNPIN, 1, "Unpin")
        }
        popup.menu.add(Menu.NONE, ID_DELETE, 2, "Delete")

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                ID_COPY -> {
                    val text = if (source == "copied") copied[position] else pinned[position]
                    safeCopyToClipboard(text)
                    Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
                    true
                }
                ID_PIN -> {
                    val text = copied[position]
                    pinItem(text)
                    removeCopiedItem(position)
                    true
                }
                ID_UNPIN -> {
                    val text = pinned[position]
                    unpinItem(text)
                    true
                }
                ID_DELETE -> {
                    if (source == "copied") removeCopiedItem(position) else removePinnedItem(position)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun refreshLists() {
        copied.clear()
        pinned.clear()
        copied.addAll(loadList("copied"))
        pinned.addAll(loadList("pinned"))
        copiedAdapter.notifyDataSetChanged()
        pinnedAdapter.notifyDataSetChanged()
        renderStatus()
    }

    private fun renderStatus() {
        val acc = isAccessibilityServiceEnabled()
        val overlay = hasOverlayPermission()
        tvStatus.text = "Accessibility: ${if (acc) "ON" else "OFF"}  •  Overlay: ${if (overlay) "ON" else "OFF"}"
    }

    private fun toggleFloating() {
        if (FloatingWidgetService.isRunning) {
            stopService(Intent(this, FloatingWidgetService::class.java))
        } else {
            startService(Intent(this, FloatingWidgetService::class.java))
        }
        renderStatus()
    }

    private fun openAccessibilitySettings() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (_: Exception) {
            Toast.makeText(this, "Không mở được trang Accessibility", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Cấp quyền Overlay rồi bấm lại", Toast.LENGTH_LONG).show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        // Chỉ check nhanh dựa theo Settings.Secure
        return Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains(packageName) == true
    }

    private fun safeCopyToClipboard(text: String) {
        try {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("text", text))
        } catch (_: Exception) { }
    }

    // ====== Local storage (SharedPreferences, khoá: copied/pinned) ======

    private fun prefs() = getSharedPreferences("clipboard_store", Context.MODE_PRIVATE)

    private fun loadList(key: String): List<String> {
        val raw = prefs().getString(key, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("\u0001").filter { it.isNotBlank() }
    }

    private fun saveList(key: String, list: List<String>) {
        val raw = list.joinToString("\u0001")
        prefs().edit().putString(key, raw).apply()
    }

    private fun addUniqueToTop(key: String, value: String) {
        if (value.isBlank()) return
        val list = loadList(key).toMutableList()
        list.remove(value)
        list.add(0, value)
        saveList(key, list)
    }

    // API dùng bởi các nơi khác
    fun addCopiedItem(value: String) {
        addUniqueToTop("copied", value)
        refreshLists()
    }

    private fun removeCopiedItem(index: Int) {
        val list = loadList("copied").toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            saveList("copied", list)
            refreshLists()
        }
    }

    private fun pinItem(value: String) {
        addUniqueToTop("pinned", value)
        Toast.makeText(this, "Pinned", Toast.LENGTH_SHORT).show()
        refreshLists()
    }

    private fun unpinItem(value: String) {
        val list = loadList("pinned").toMutableList()
        list.remove(value)
        saveList("pinned", list)
        Toast.makeText(this, "Unpinned", Toast.LENGTH_SHORT).show()
        refreshLists()
    }

    private fun removePinnedItem(index: Int) {
        val list = loadList("pinned").toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            saveList("pinned", list)
            refreshLists()
        }
    }
}
