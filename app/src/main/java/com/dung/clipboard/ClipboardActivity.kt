package com.dung.clipboard

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClipboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(Color.argb(200, 0, 150, 136))
        layout.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val title = TextView(this)
        title.text = "📋 ĐÃ COPY"
        title.setTextColor(Color.WHITE)
        title.textSize = 24f
        title.gravity = Gravity.CENTER
        layout.addView(title)

        setContentView(layout)
    }
}
