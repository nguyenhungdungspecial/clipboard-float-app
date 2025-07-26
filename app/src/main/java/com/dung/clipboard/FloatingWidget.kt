package com.dung.clipboard

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.ImageView
import android.view.View.OnTouchListener
import android.view.WindowManager

class FloatingWidget(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var floatingView: ImageView? = null
    private var isActivityOpen = false

    fun show() {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        floatingView = ImageView(context).apply {
            setImageResource(android.R.drawable.btn_star_big_on)
            layoutParams = ViewGroup.LayoutParams(150, 150)
            setBackgroundColor(0x55FFFFFF)
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.END
        layoutParams.x = 100
        layoutParams.y = 300

        floatingView?.setOnTouchListener(FloatingTouchListener(layoutParams))

        floatingView?.setOnClickListener {
            if (!isActivityOpen) {
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                isActivityOpen = true
            } else {
                val closeIntent = Intent("com.dung.clipboard.CLOSE_ACTIVITY")
                context.sendBroadcast(closeIntent)
                isActivityOpen = false
            }
        }

        windowManager?.addView(floatingView, layoutParams)
    }

    inner class FloatingTouchListener(private val layoutParams: WindowManager.LayoutParams) : OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX - (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(view, layoutParams)
                    return true
                }
            }
            return false
        }
    }
}
