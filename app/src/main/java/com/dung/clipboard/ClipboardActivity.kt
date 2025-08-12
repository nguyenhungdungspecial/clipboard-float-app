package com.dung.clipboard

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dung.clipboard.databinding.ActivityClipboardBinding

class ClipboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClipboardBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: ClipboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClipboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("clipboard_data", MODE_PRIVATE)

        val savedItems = prefs.getStringSet("items", emptySet())?.toMutableList() ?: mutableListOf()
        adapter = ClipboardAdapter(savedItems)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Nút xóa toàn bộ clipboard history
        binding.btnClear.setOnClickListener {
            savedItems.clear()
            adapter.notifyDataSetChanged()
            prefs.edit().putStringSet("items", savedItems.toSet()).apply()
        }
    }

    fun addClipboardItem(text: String) {
        val savedItems = prefs.getStringSet("items", emptySet())?.toMutableList() ?: mutableListOf()
        if (!savedItems.contains(text)) {
            savedItems.add(0, text)
            prefs.edit().putStringSet("items", savedItems.toSet()).apply()
            adapter.updateData(savedItems)
        }
    }
}
