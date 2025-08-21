package com.dung.clipboard

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView

class FloatingWidgetService : Service() {

    companion object {
        @Volatile
        var isRunning = false
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addFloatingWidgetIcon()
    }

    private fun addFloatingWidgetIcon() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Đảm bảo sử dụng đúng layout chỉ có icon ngôi sao
        floatingView = inflater.inflate(R.layout.floating_widget_icon_layout, null)
        val btnStar = floatingView!!.findViewById<ImageView>(R.id.btnStar)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params!!.gravity = Gravity.TOP or Gravity.START
        params!!.x = 30
        params!!.y = 120

        windowManager!!.addView(floatingView, params)

        btnStar.setOnClickListener {
            // Khi nhấn vào icon, bật/tắt FloatingContentService
            val intent = Intent(this, FloatingContentService::class.java)
            if (FloatingContentService.isRunning) {
                stopService(intent)
            } else {
                startService(intent)
            }
        }

        // Thêm OnTouchListener để di chuyển view.
        // **Lắng nghe trên floatingView (View gốc) chứ không phải btnStar**
        floatingView!!.setOnTouchListener(View.OnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params!!.x
                    initialY = params!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return@OnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                    params!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager!!.updateViewLayout(floatingView, params)
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    override fun onDestroy() {
        isRunning = false
        if (floatingView != null) windowManager?.removeView(floatingView)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

