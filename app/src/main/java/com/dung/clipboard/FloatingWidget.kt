package com.dung.clipboard

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import com.dung.clipboard.R // Đảm bảo đã import R

class FloatingWidget(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var floatingView: ImageView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    init {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.d("FloatingWidget", "init: FloatingWidget initialized")
    }

    fun show() {
        Log.d("FloatingWidget", "show: Attempting to show widget")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Log.w("FloatingWidget", "show: No SYSTEM_ALERT_WINDOW permission")
            return
        }

        if (floatingView == null) {
            floatingView = ImageView(context).apply {
                // Sử dụng icon ngôi sao từ Android
                setImageResource(android.R.drawable.btn_star_big_on) 
                layoutParams = ViewGroup.LayoutParams(150, 150)
                setBackgroundColor(Color.parseColor("#80FFFFFF"))
            }
            Log.d("FloatingWidget", "show: floatingView created")
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }
        Log.d("FloatingWidget", "show: LayoutParams set")

        floatingView?.setOnTouchListener(FloatingTouchListener(layoutParams!!))

        floatingView?.setOnClickListener {
            Log.d("FloatingWidget", "Widget clicked, opening MainActivity.")
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }

        try {
            if (floatingView?.windowToken == null) {
                windowManager?.addView(floatingView, layoutParams)
                Log.d("FloatingWidget", "show: Widget added to WindowManager")
            } else {
                windowManager?.updateViewLayout(floatingView, layoutParams)
                Log.d("FloatingWidget", "show: Widget updated in WindowManager")
            }
        } catch (e: Exception) {
            Log.e("FloatingWidget", "show: Error adding/updating widget", e)
            e.printStackTrace()
        }
    }

    fun remove() {
        Log.d("FloatingWidget", "remove: Attempting to remove widget")
        if (floatingView != null && windowManager != null && floatingView?.windowToken != null) {
            try {
                windowManager?.removeView(floatingView)
                Log.d("FloatingWidget", "remove: Widget removed from WindowManager")
            } catch (e: IllegalArgumentException) {
                Log.e("FloatingWidget", "remove: Error removing widget (not attached?)", e)
                e.printStackTrace()
            }
            floatingView = null
            layoutParams = null
        } else {
            Log.d("FloatingWidget", "remove: Widget or WindowManager is null, or widget not attached.")
        }
    }

    inner class FloatingTouchListener(private val layoutParams: WindowManager.LayoutParams) : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var isClick = true

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        view.performClick()
                    }
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()

                    if (isClick && (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10)) {
                        isClick = false
                    }

                    if (!isClick) {
                        layoutParams.x = initialX + deltaX
                        layoutParams.y = initialY + deltaY
                        windowManager?.updateViewLayout(view, layoutParams)
                    }
                    return true
                }

                else -> return false
            }
        }
    }
}

