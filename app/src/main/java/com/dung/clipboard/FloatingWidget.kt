package com.dung.clipboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView

class FloatingWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val tv: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.floating_widget_layout, this, true)
        tv = findViewById(R.id.clipboard_text)
    }

    fun setText(text: String) {
        tv.text = text
    }
}
