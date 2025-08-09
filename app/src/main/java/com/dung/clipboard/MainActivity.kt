package com.dung.clipboard

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var var toggleServiceButton: Button

    private val clipboardReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val copiedData = intent?.getStringExtra("copied_data")
            if (copiedData != null) {
                // TODO: Cập nhật giao diện của MainActivity ở đây
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkOverlayPermission() // Gọi hàm kiểm tra quyền

        toggleServiceButton = findViewById(R.id.toggleServiceButton)

        toggleServiceButton.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                if (isServiceRunning(FloatingWidgetService::class.java)) {
                    val intent = Intent(this, FloatingWidgetService::class.java)
                    stopService(intent)
                    toggleServiceButton.text = "Bật Clipboard Nổi"
                } else {
                    val intent = Intent(this, FloatingWidgetService::class.java)
                    startService(intent)
                    toggleServiceButton.text = "Tắt Clipboard Nổi"
                }
            } else {
                checkOverlayPermission() // Yêu cầu quyền nếu chưa có
            }
        }

        val clipboardServiceIntent = Intent(this, ClipboardService::class.java)
        startService(clipboardServiceIntent)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("com.dung.clipboard.CLIPBOARD_UPDATE")
        registerReceiver(clipboardReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(clipboardReceiver)
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("Yêu cầu quyền")
                .setMessage("Ứng dụng cần quyền 'Hiển thị trên các ứng dụng khác' để chạy cửa sổ nổi.")
                .setPositiveButton("Đi đến cài đặt") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
                .setNegativeButton("Hủy") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }
    
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}

