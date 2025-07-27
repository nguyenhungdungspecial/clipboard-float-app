package com.dung.clipboard

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.ImageView
import android.provider.Settings // Thêm import này cho Settings

class FloatingWidget(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var floatingView: ImageView? = null
    // isActivityOpen không cần thiết nếu bạn dùng FLAG_ACTIVITY_REORDER_TO_FRONT

    init { // Khởi tạo windowManager ngay khi FloatingWidget được tạo
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun show() {
        // Kiểm tra quyền vẽ đè trước khi thêm view
        // Mặc dù quyền này nên được kiểm tra trước khi khởi động dịch vụ,
        // việc kiểm tra lại ở đây giúp đảm bảo an toàn.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            // Nếu không có quyền, không hiển thị widget.
            // Trong môi trường Service, Toast sẽ không hoạt động trực tiếp trên UI của app.
            // Có thể dùng Log hoặc một cách thông báo khác.
            return
        }

        floatingView = ImageView(context).apply {
            setImageResource(android.R.drawable.btn_star_big_on) // Đổi icon nếu muốn
            layoutParams = ViewGroup.LayoutParams(150, 150) // Kích thước của widget
            setBackgroundColor(0x55FFFFFF) // Màu nền trong suốt một phần
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // Thêm FLAG_LAYOUT_NO_LIMITS để cho phép widget ra ngoài màn hình một chút
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.END // Vị trí mặc định ở góc trên bên phải
        layoutParams.x = 100 // Lề phải
        layoutParams.y = 300 // Lề trên

        floatingView?.setOnTouchListener(FloatingTouchListener(layoutParams))

        floatingView?.setOnClickListener {
            // Khi click vào widget, mở MainActivity.
            // FLAG_ACTIVITY_NEW_TASK: Cần thiết vì bạn đang khởi chạy Activity từ một Context không phải Activity.
            // FLAG_ACTIVITY_REORDER_TO_FRONT: Nếu MainActivity đã chạy, nó sẽ được đưa lên đầu, không tạo instance mới.
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            context.startActivity(intent)
        }

        try {
            windowManager?.addView(floatingView, layoutParams)
        } catch (e: Exception) {
            // Xử lý lỗi nếu không thể thêm view (ví dụ: thiếu quyền, mặc dù đã kiểm tra)
            e.printStackTrace()
        }
    }

    // Phương thức để loại bỏ widget khỏi màn hình
    fun remove() {
        if (floatingView != null && windowManager != null) {
            try {
                windowManager?.removeView(floatingView)
            } catch (e: IllegalArgumentException) {
                // Xử lý trường hợp view không được gắn vào window manager (ví dụ: đã bị remove trước đó)
                e.printStackTrace()
            }
            floatingView = null
            // windowManager có thể giữ lại hoặc đặt lại null tùy thuộc vào logic ứng dụng
            // Đặt lại null để giải phóng tài nguyên
            windowManager = null
        }
    }

    // Lớp lắng nghe sự kiện chạm để di chuyển widget
    inner class FloatingTouchListener(private val layoutParams: WindowManager.LayoutParams) :
        View.OnTouchListener {
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
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(view, layoutParams)
                    return true
                }
                // Bạn có thể thêm ACTION_UP để lưu vị trí cuối cùng của widget nếu muốn
            }
            return false
        }
    }
}

