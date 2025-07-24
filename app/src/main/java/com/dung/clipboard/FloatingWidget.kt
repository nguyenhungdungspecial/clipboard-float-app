package com.dung.clipboard

import android.content.*
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.*
import android.view.WindowManager.LayoutParams

class FloatingWidget(private val context: Context) {
    private lateinit var windowManager: WindowManager
    private lateinit var icon: View

    fun show() {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        icon = ImageView(context).apply {
            setImageResource(android.R.drawable.btn_star_big_on) // icon tạm
        }

        val params = LayoutParams(150, 150,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                LayoutParams.TYPE_APPLICATION_OVERLAY
            else LayoutParams.TYPE_PHONE,
            LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        icon.setOnClickListener {
            val i = Intent(context, ClipboardActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }

        windowManager.addView(icon, params)
    }
}
