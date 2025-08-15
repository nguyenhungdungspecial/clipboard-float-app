package com.dung.clipboard

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import kotlin.math.abs

class FloatingWidgetService : Service() {

    companion object { @Volatile var isRunning = false }

    private var wm: WindowManager? = null
    private var view: View? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        addStar()
    }

    private fun addStar() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.floating_widget_layout, null)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40; y = 140
        }

        wm?.addView(view, params)

        val star = view!!.findViewById<ImageView>(R.id.btnStar)

        // click vs drag phân biệt bằng delta
        var downX = 0f; var downY = 0f
        var startX = 0; var startY = 0
        var moved = false

        view!!.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    moved = false
                    downX = e.rawX; downY = e.rawY
                    startX = params!!.x; startY = params!!.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - downX).toInt()
                    val dy = (e.rawY - downY).toInt()
                    if (abs(dx) > 5 || abs(dy) > 5) moved = true
                    params!!.x = startX + dx
                    params!!.y = startY + dy
                    runCatching { wm?.updateViewLayout(view, params) }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) toggleMainActivity()
                    true
                }
                else -> false
            }
        }

        // đề phòng user chạm trực tiếp vào hình (một số máy không chuyển sự kiện cho parent)
        star.setOnClickListener { toggleMainActivity() }
    }

    private fun toggleMainActivity() {
        if (ActivityVisibility.visible) {
            // gửi lệnh đóng
            sendBroadcast(Intent(ClipboardStorage.ACTION_CLOSE_ACTIVITY))
        } else {
            val i = Intent(this, ClipboardActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(i)
        }
    }

    override fun onDestroy() {
        isRunning = false
        runCatching { if (view != null) wm?.removeView(view) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
