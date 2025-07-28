package com.dung.clipboard

import android.content.Context
import android.content.Intent
import android.graphics.Color // Đảm bảo đã import Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView

class FloatingWidget(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var floatingView: ImageView? = null
    private var layoutParams: WindowManager.LayoutParams? = null // Khai báo layoutParams ở đây

    init {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun show() {
        // Kiểm tra quyền vẽ đè
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            // Không hiển thị widget nếu thiếu quyền
            return
        }

        if (floatingView == null) { // Chỉ tạo floatingView nếu chưa có
            floatingView = ImageView(context).apply {
                setImageResource(android.R.drawable.btn_star_big_on) // Icon bạn đang dùng
                layoutParams = ViewGroup.LayoutParams(150, 150) // Kích thước của widget
                setBackgroundColor(Color.parseColor("#80FFFFFF")) // Màu nền trong suốt một phần (mã Hex #AARRGGBB)
                                                                 // #80 = 50% alpha, FFFFFF = trắng
            }
        }


        // Setup WindowManager.LayoutParams
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
            gravity = Gravity.TOP or Gravity.END // Vị trí mặc định ở góc trên bên phải
            x = 100 // Lề phải
            y = 300 // Lề trên
        }

        // Thiết lập OnTouchListener để di chuyển widget
        floatingView?.setOnTouchListener(FloatingTouchListener(layoutParams!!)) // Dùng !! vì đã kiểm tra null ở trên

        // Thiết lập OnClickListener cho icon widget
        floatingView?.setOnClickListener {
            // Khi click vào widget, mở MainActivity
            // FLAG_ACTIVITY_NEW_TASK: Cần thiết vì bạn đang khởi chạy Activity từ một Context không phải Activity.
            // FLAG_ACTIVITY_REORDER_TO_FRONT: Nếu MainActivity đã chạy, nó sẽ được đưa lên đầu, không tạo instance mới.
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            context.startActivity(intent)
            // Bạn có thể cân nhắc ẩn widget sau khi mở MainActivity nếu muốn:
            // remove()
        }

        try {
            if (floatingView?.windowToken == null) { // Chỉ thêm nếu chưa được thêm
                windowManager?.addView(floatingView, layoutParams)
            } else {
                windowManager?.updateViewLayout(floatingView, layoutParams) // Cập nhật nếu đã thêm
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Phương thức để loại bỏ widget khỏi màn hình
    fun remove() {
        if (floatingView != null && windowManager != null && floatingView?.windowToken != null) {
            try {
                windowManager?.removeView(floatingView)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            floatingView = null // Đặt lại null để giải phóng tài nguyên
            layoutParams = null // Đặt lại null cho layoutParams
        }
    }

    // Lớp lắng nghe sự kiện chạm để di chuyển widget
    // Đã sửa để xử lý ACTION_UP để phân biệt click và kéo
    inner class FloatingTouchListener(private val layoutParams: WindowManager.LayoutParams) : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var isClick = false // Biến để kiểm tra liệu đó có phải là click hay không

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true // Giả định ban đầu là click
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    // Nếu không di chuyển đáng kể, coi là click
                    if (isClick && (Math.abs(event.rawX - initialTouchX) < 10 && Math.abs(event.rawY - initialTouchY) < 10)) {
                        view.performClick() // Kích hoạt OnClickListener
                    }
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(view, layoutParams)
                    isClick = false // Đã di chuyển, không còn là click nữa
                    return true
                }
            }
            return false
        }
    }
}

