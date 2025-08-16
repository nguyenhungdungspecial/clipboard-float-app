package com.dung.clipboard

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var lvCopied: ListView
    private lateinit var lvPinned: ListView
    private lateinit var tvStatus: TextView
    private lateinit var btnRequestAccessibility: Button
    private lateinit var btnToggleFloat: Button
    private lateinit var btnClear: Button
    private lateinit var tvEmptyCopied: TextView
    private lateinit var tvEmptyPinned: TextView

    private lateinit var copiedAdapter: ArrayAdapter<String>
    private lateinit var pinnedAdapter: ArrayAdapter<String>
    private val copied = mutableListOf<String>()
    private val pinned = mutableListOf<String>()

    private val ID_COPY = 1
    private val ID_PIN = 2
    private val ID_UNPIN = 3
    private val ID_DELETE = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lvCopied = findViewById(R.id.lvCopied)
        lvPinned = findViewById(R.id.lvPinned)
        tvStatus = findViewById(R.id.tvStatus)
        btnRequestAccessibility = findViewById(R.id.btnRequestAccessibility)
        btnToggleFloat = findViewById(R.id.btnToggleFloat)
        btnClear = findViewById(R.id.btnClear)
        tvEmptyCopied = findViewById(R.id.tvEmptyCopied)
        tvEmptyPinned = findViewById(R.id.tvEmptyPinned)

        copiedAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, copied)
        pinnedAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, pinned)
        lvCopied.adapter = copiedAdapter
        lvPinned.adapter = pinnedAdapter

        attachEmptyView(lvCopied, tvEmptyCopied)
        attachEmptyView(lvPinned, tvEmptyPinned)

        refreshLists()
        renderStatus()

        btnRequestAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }

        btnToggleFloat.setOnClickListener {
            if (!hasOverlayPermission()) {
                requestOverlayPermission()
            } else {
                toggleFloating()
            }
        }

        btnClear.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Xoá lịch sử")
                .setMessage("Xoá toàn bộ danh sách Copied?")
                .setPositiveButton("Xoá") { _, _ ->
                    saveList("copied", emptyList())
                    refreshLists()
                }
                .setNegativeButton("Huỷ", null)
                .show()
        }

        lvCopied.setOnItemClickListener { _, _, pos, _ ->
            safeCopyToClipboard(copied[pos])
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
        }
        lvPinned.setOnItemClickListener { _, _, pos, _ ->
            safeCopyToClipboard(pinned[pos])
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
        }

        lvCopied.setOnItemLongClickListener { v, _, pos, _ ->
            showItemMenu(v, source = "copied", position = pos)
            true
        }
        lvPinned.setOnItemLongClickListener { v, _, pos, _ ->
            showItemMenu(v, source = "pinned", position = pos)
            true
        }
    }

    private fun attachEmptyView(listView: ListView, emptyView: TextView) {
        listView.emptyView = emptyView
    }

    private fun showItemMenu(anchor: View, source: String, position: Int) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(Menu.NONE, ID_COPY, 0, "Copy")
        if (source == "copied") {
            popup.menu.add(Menu.NONE, ID_PIN, 1, "Pin")
        } else {
            popup.menu.add(Menu.NONE, ID_UNPIN, 1, "Unpin")
        }
        popup.menu.add(Menu.NONE, ID_DELETE, 2, "Delete")

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                ID_COPY -> {
                    val text = if (source == "copied") copied[position] else pinned[position]
                    safeCopyToClipboard(text)
                    Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
                    true
                }

