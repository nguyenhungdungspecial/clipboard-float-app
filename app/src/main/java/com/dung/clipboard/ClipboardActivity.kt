package com.dung.clipboard

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClipboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        ClipboardDataManager.copiedItems.forEach {
            val view = TextView(this)
            view.text = it
            layout.addView(view)
        }

        ClipboardDataManager.pinnedItems.forEach {
            val view = TextView(this)
            view.text = "📌 $it"
            layout.addView(view)
        }

        setContentView(layout)
    }
}
