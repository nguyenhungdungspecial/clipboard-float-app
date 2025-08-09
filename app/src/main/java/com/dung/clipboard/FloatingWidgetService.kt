package com.dung.clipboard

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView

class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingWidgetView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var clipboardTextView: TextView
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f

    private val clipboardReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val copiedData = intent?.getStringExtra("copied_data")
            if (copiedData != null) {
                clipboardTextView.text = copiedData
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingWidgetView = inflater.inflate(R.layout.floating_widget_layout, null)

        clipboardTextView = floatingWidgetView.findViewById(R.id.clipboardTextView)
        val closeButton = floatingWidgetView.findViewById<ImageButton>(R.id.closeButton)

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 100

        windowManager.addView(floatingWidgetView, layoutParams)

        closeButton.setOnClickListener {
            stopSelf()
        }

        floatingWidgetView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingWidgetView, layoutParams)
                        return true
                    }
                    else -> return false
                }
            }
        })

        val filter = IntentFilter("com.dung.clipboard.CLIPBOARD_UPDATE")
        registerReceiver(clipboardReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(clipboardReceiver)

        if (::floatingWidgetView.isInitialized) {
            windowManager.removeView(floatingWidgetView)
        }
    }
}

