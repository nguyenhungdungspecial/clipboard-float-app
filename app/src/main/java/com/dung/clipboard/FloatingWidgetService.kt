package com.dung.clipboard

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import kotlin.math.abs

/**
 * Icon ngôi sao nổi: click để mở/đóng Activity, kéo để di chuyển.
 * Không phụ thuộc layout XML (tạo view bằng code để bạn khỏi sửa resource).
 */
class FloatingWidgetService : Service() {

    companion object { @Volatile var isRunning: Boolean = false }

    private var wm: WindowManager? = null
    private var view: View? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        addStarView()
    }

    private fun addStarView() {
        val img = ImageView(this).apply {
            // dùng icon hệ thống để khỏi thêm drawable
            setImageResource(android.R.drawable.btn_star_big_on)
            isClickable = true
        }
        view = img

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
            x = 48; y = 160
        }

        wm?.addView(view, params)

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

        img.setOnClickListener {
            // dự phòng một số máy không gọi ACTION_UP khi click
            if (!moved) toggleMainActivity()
        }
    }

    private fun toggleMainActivity() {
        if (ClipboardActivity.isVisible) {
            sendBroadcast(Intent(ACTION_CLOSE_ACTIVITY))
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
