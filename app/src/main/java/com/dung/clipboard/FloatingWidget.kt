package com.dung.clipboard

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
                setImageResource(android.R.drawable.btn_star_big_on)
                // ĐÃ XÓA: layoutParams = ViewGroup.LayoutParams(150, 150) ở đây
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
            150, // Chiều rộng của widget
            150, // Chiều cao của widget
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 100
            y = 300
        }
        Log.d("FloatingWidget", "show: LayoutParams set")

        floatingView?.setOnTouchListener(FloatingTouchListener(layoutParams!!))

        floatingView?.setOnClickListener {
            Log.d("FloatingWidget", "onClick: Floating widget clicked, opening MainActivity")
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
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
        private var isClick = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    Log.d("FloatingWidget", "onTouch: ACTION_DOWN")
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (isClick && (Math.abs(event.rawX - initialTouchX) < 10 && Math.abs(event.rawY - initialTouchY) < 10)) {
                        view.performClick()
                        Log.d("FloatingWidget", "onTouch: ACTION_UP - Performing Click")
                    } else {
                        Log.d("FloatingWidget", "onTouch: ACTION_UP - Not a click (moved too much)")
                    }
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(view, layoutParams)
                    isClick = false
                    // Log.d("FloatingWidget", "onTouch: ACTION_MOVE - x=${layoutParams.x}, y=${layoutParams.y}")
                    return true
                }
            }
            return false
        }
    }
}

