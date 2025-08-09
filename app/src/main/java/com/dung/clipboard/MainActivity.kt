package com.dung.clipboard

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var toggleServiceButton: Button

    private val clipboardReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val copiedData = intent?.getStringExtra("copied_data")
            if (copiedData != null) {
                // TODO: Cập nhật giao diện của MainActivity ở đây
                // Ví dụ: cập nhật một TextView hoặc RecyclerView
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleServiceButton = findViewById(R.id.toggleServiceButton)

        toggleServiceButton.setOnClickListener {
            if (isServiceRunning(FloatingWidgetService::class.java)) {
                val intent = Intent(this, FloatingWidgetService::class.java)
                stopService(intent)
                toggleServiceButton.text = "Bật Clipboard Nổi"
            } else {
                val intent = Intent(this, FloatingWidgetService::class.java)
                startService(intent)
                toggleServiceButton.text = "Tắt Clipboard Nổi"
            }
        }

        // Khởi động ClipboardService để lắng nghe clipboard
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

