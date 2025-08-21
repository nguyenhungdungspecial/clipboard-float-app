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
    private var isClick = true // Biến cờ để phân biệt sự kiện kéo và nhấn

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addFloatingWidgetIcon()
    }

    private fun addFloatingWidgetIcon() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
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

        // **Sửa đổi logic OnTouchListener để xử lý cả nhấn và kéo**
        floatingView!!.setOnTouchListener(View.OnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params!!.x
                    initialY = params!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true // Reset cờ nhấn
                    return@OnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Nếu di chuyển đủ xa, coi là kéo chứ không phải nhấn
                    if (Math.abs(event.rawX - initialTouchX) > 10 || Math.abs(event.rawY - initialTouchY) > 10) {
                        isClick = false
                    }
                    params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                    params!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager!!.updateViewLayout(floatingView, params)
                    return@OnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    // Nếu là sự kiện nhấn, kích hoạt hành động
                    if (isClick) {
                        val intent = Intent(this, FloatingContentService::class.java)
                        if (FloatingContentService.isRunning) {
                            stopService(intent)
                        } else {
                            startService(intent)
                        }
                    }
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

