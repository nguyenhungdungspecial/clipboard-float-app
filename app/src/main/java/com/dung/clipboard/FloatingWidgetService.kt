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

    private var xDelta = 0
    private var yDelta = 0

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addFloatingWidgetView()
    }

    private fun addFloatingWidgetView() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_widget_layout, null)

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
        params!!.x = 0
        params!!.y = 100

        windowManager!!.addView(floatingView, params)

        val ivIcon = floatingView!!.findViewById<ImageView>(R.id.btnStar)
        ivIcon.setOnClickListener {
            // Khi nhấn vào icon, gửi broadcast để mở FloatingContentService
            val intent = Intent(this, FloatingContentService::class.java)
            if (!FloatingContentService.isRunning) {
                startService(intent)
            } else {
                stopService(intent)
            }
        }

        // Thêm OnTouchListener để di chuyển view
        floatingView!!.setOnTouchListener(View.OnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    xDelta = params!!.x - event.rawX.toInt()
                    yDelta = params!!.y - event.rawY.toInt()
                    return@OnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    params!!.x = event.rawX.toInt() + xDelta
                    params!!.y = event.rawY.toInt() + yDelta
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

