package com.dung.clipboard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClipboardActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var lvCopied: ListView
    private lateinit var tvStatus: TextView
    private lateinit var btnClear: Button
    private lateinit var adapter: android.widget.ArrayAdapter<String>
    private val items = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // reuse activity_main

        prefs = getSharedPreferences("clipboard_data", Context.MODE_PRIVATE)

        lvCopied = findViewById(R.id.lvCopied)
        tvStatus = findViewById(R.id.tvStatus)
        btnClear = findViewById(R.id.btnClear)

        loadItems()
        adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        lvCopied.adapter = adapter
        updateStatus()

        // Handle possible new clip sent by service
        val newClip = intent.getStringExtra("new_clip")
        if (!newClip.isNullOrEmpty()) {
            addClipboardItem(newClip)
        }

        btnClear.setOnClickListener {
            items.clear()
            saveItems()
            adapter.notifyDataSetChanged()
            updateStatus()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Called if activity is already running and service starts it again
        val newClip = intent?.getStringExtra("new_clip")
        if (!newClip.isNullOrEmpty()) addClipboardItem(newClip)
    }

    private fun loadItems() {
        val set = prefs.getStringSet("items", emptySet()) ?: emptySet()
        items.clear()
        items.addAll(set.toList().reversed()) // newest first
    }

    private fun saveItems() {
        prefs.edit().putStringSet("items", items.reversed().toSet()).apply()
    }

    fun addClipboardItem(text: String) {
        if (text.isBlank()) return
        if (items.isNotEmpty() && items[0] == text) return
        items.add(0, text)
        if (items.size > 200) items.removeLast()
        saveItems()
        adapter.notifyDataSetChanged()
        updateStatus()
    }

    private fun updateStatus() {
        tvStatus.text = if (items.isEmpty()) "Clipboard rỗng" else "Có ${items.size} mục"
    }
}
