package com.dung.clipboard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageButton
import androidx.core.app.NotificationCompat

class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private var params: WindowManager.LayoutParams? = null
    private var widget: FloatingWidget? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startForeground(1, buildNotification())

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        widget = FloatingWidget(this)

        widget?.setOnPinClick { text ->
            // pin vào danh sách
            val prefs = getSharedPreferences("clipboard_store", Context.MODE_PRIVATE)
            val raw = prefs.getString("pinned", "") ?: ""
            val list = if (raw.isBlank()) mutableListOf() else raw.split("\u0001").toMutableList()
            list.remove(text)
            list.add(0, text)
            prefs.edit().putString("pinned", list.joinToString("\u0001")).apply()
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        val root = widget!!.root

        // drag to move
        root.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                event ?: return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params!!.x
                        initialY = params!!.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                        params!!.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(root, params)
                        return true
                    }
                }
                return false
            }
        })

        // close button (nếu có trong layout)
        root.findViewById<ImageButton?>(R.id.btnClose)?.setOnClickListener {
            stopSelf()
        }

        windowManager.addView(root, params)
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            windowManager.removeView(widget?.root)
        } catch (_: Exception) {}
        isRunning = false
        instance = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("float", "Floating", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        return NotificationCompat.Builder(this, "float")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Clipboard Float")
            .setContentText("Floating widget đang chạy")
            .setContentIntent(intent)
            .build()
    }

    fun updateText(text: String) {
        widget?.setText(text)
    }

    companion object {
        @Volatile var isRunning = false
            private set

        @Volatile private var instance: FloatingWidgetService? = null

        fun updateClipboardText(text: String) {
            instance?.updateText(text)
        }
    }
}
