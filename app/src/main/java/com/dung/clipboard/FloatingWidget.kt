package com.dung.clipboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast

object FloatingWidget {
    private var textView: TextView? = null

    fun create(context: Context): View {
        val view = LayoutInflater.from(context).inflate(R.layout.floating_widget, null)
        textView = view.findViewById(R.id.clipboard_text)
        view.setOnClickListener {
            Toast.makeText(context, "Floating Widget clicked", Toast.LENGTH_SHORT).show()
        }
        return view
    }

    fun updateClipboardText(text: String) {
        textView?.text = text
    }
}
