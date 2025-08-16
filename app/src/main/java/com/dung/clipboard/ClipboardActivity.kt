package com.dung.clipboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ClipboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dùng lại layout chính để tránh mismatch ID
        setContentView(R.layout.activity_main)
        // Có thể điều hướng về MainActivity nếu cần
        finish()
    }
}
