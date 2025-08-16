package com.dung.clipboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView

class FloatingWidget(
    context: Context
) {
    val root: View = LayoutInflater.from(context).inflate(R.layout.floating_widget_layout, null)
    private val tv: TextView? = root.findViewById(R.id.tvClipboard)
    private val btnStar: ImageButton? = root.findViewById(R.id.btnStar)

    fun setText(text: String) {
        tv?.text = text
    }

    fun setOnPinClick(action: (String) -> Unit) {
        btnStar?.setOnClickListener {
            val t = tv?.text?.toString().orEmpty()
            if (t.isNotBlank()) action(t)
        }
    }
}
