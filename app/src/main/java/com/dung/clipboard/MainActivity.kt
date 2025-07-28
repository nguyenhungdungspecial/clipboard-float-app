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
import android.util.Log // THÊM DÒNG NÀY
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dung.clipboard.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var clipboard: ClipboardManager
    private var isServiceRunning = false

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate: Activity created") // THÊM LOG
        ClipboardDataManager.initialize(this) // Đảm bảo dòng này ở đây
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // Listener này chỉ cập nhật khi MainActivity đang hiển thị.
        // Listener chính trong FloatingWidgetService mới là quan trọng để cập nhật khi app ở nền.
        clipboard.addPrimaryClipChangedListener {
            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (!clipText.isNullOrBlank()) {
                Log.d("MainActivity", "Clip changed in MainActivity: $clipText") // THÊM LOG
                ClipboardDataManager.addCopy(clipText)
                recreate() // Tải lại Activity để cập nhật danh sách
            }
        }

        binding.toggleServiceButton.setOnClickListener {
            if (isServiceRunning) {
                stopFloatingWidgetService()
                Toast.makeText(this@MainActivity, "Đã tắt Clipboard Nổi", Toast.LENGTH_SHORT).show()
                Log.d("MainActivity", "Toggle service: Stopping service") // THÊM LOG
            } else {
                startFloatingWidgetService()
                Log.d("MainActivity", "Toggle service: Starting service") // THÊM LOG
            }
        }

        addCopiedAndPinnedItems()
        isServiceRunning = isMyServiceRunning(FloatingWidgetService::class.java)
        updateToggleButtonText()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume: Activity resumed, refreshing items") // THÊM LOG
        // Đảm bảo dữ liệu được tải lại khi Activity quay lại foreground
        ClipboardDataManager.initialize(this) // Gọi lại để đảm bảo dữ liệu được tải mới nhất
        addCopiedAndPinnedItems()
        isServiceRunning = isMyServiceRunning(FloatingWidgetService::class.java)
        updateToggleButtonText()
    }

    private fun addCopiedAndPinnedItems() {
        Log.d("MainActivity", "addCopiedAndPinnedItems: Refreshing lists") // THÊM LOG
        if (binding.copiedLayout.childCount > 1) {
            binding.copiedLayout.removeViews(1, binding.copiedLayout.childCount - 1)
        }
        if (binding.pinnedLayout.childCount > 1) {
            binding.pinnedLayout.removeViews(1, binding.pinnedLayout.childCount - 1)
        }

        ClipboardDataManager.getCopiedList().forEach { text ->
            binding.copiedLayout.addView(createTextItem(text, false))
            Log.d("MainActivity", "Added copied item: $text") // THÊM LOG
        }

        ClipboardDataManager.getPinnedList().forEach { text ->
            binding.pinnedLayout.addView(createTextItem(text, true))
            Log.d("MainActivity", "Added pinned item: $text") // THÊM LOG
        }
    }

    // Các hàm khác giữ nguyên
    private fun startFloatingWidgetService() { /* ... */ }
    private fun stopFloatingWidgetService() { /* ... */ }
    private fun updateToggleButtonText() { /* ... */ }
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean { /* ... */ }
    private fun createTextItem(text: String, isPinned: Boolean): LinearLayout { /* ... */ }
}

