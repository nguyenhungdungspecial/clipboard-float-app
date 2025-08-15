package com.dung.clipboard

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var listViewCopied: ListView
    private lateinit var listViewPinned: ListView
    private lateinit var adapterCopied: ArrayAdapter<String>
    private lateinit var adapterPinned: ArrayAdapter<String>

    private lateinit var clipboardDataManager: ClipboardDataManager

    private var selectedItem: String? = null
    private var selectedListType: String? = null // "copied" hoặc "pinned"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clipboardDataManager = ClipboardDataManager(this)

        listViewCopied = findViewById(R.id.listViewCopied)
        listViewPinned = findViewById(R.id.listViewPinned)

        val emptyViewCopied = findViewById<View>(R.id.empty_view_copied)
        val emptyViewPinned = findViewById<View>(R.id.empty_view_pinned)

        listViewCopied.emptyView = emptyViewCopied
        listViewPinned.emptyView = emptyViewPinned

        adapterCopied = ArrayAdapter(this, android.R.layout.simple_list_item_1, clipboardDataManager.getCopiedList())
        adapterPinned = ArrayAdapter(this, android.R.layout.simple_list_item_1, clipboardDataManager.getPinnedList())

        listViewCopied.adapter = adapterCopied
        listViewPinned.adapter = adapterPinned

        registerForContextMenu(listViewCopied)
        registerForContextMenu(listViewPinned)

        listViewCopied.setOnItemClickListener { _, _, position, _ ->
            selectedItem = adapterCopied.getItem(position)
            selectedListType = "copied"
            openContextMenu(listViewCopied)
        }

        listViewPinned.setOnItemClickListener { _, _, position, _ ->
            selectedItem = adapterPinned.getItem(position)
            selectedListType = "pinned"
            openContextMenu(listViewPinned)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_copy -> {
                selectedItem?.let {
                    Utils.copyToClipboard(this, it)
                    Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.menu_pin -> {
                selectedItem?.let {
                    clipboardDataManager.pinItem(it)
                    refreshLists()
                    Toast.makeText(this, "Pinned", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.menu_unpin -> {
                selectedItem?.let {
                    clipboardDataManager.unpinItem(it)
                    refreshLists()
                    Toast.makeText(this, "Unpinned", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.menu_delete -> {
                selectedItem?.let {
                    if (selectedListType == "copied") {
                        clipboardDataManager.removeCopiedItem(it)
                    } else {
                        clipboardDataManager.removePinnedItem(it)
                    }
                    refreshLists()
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun refreshLists() {
        adapterCopied.clear()
        adapterCopied.addAll(clipboardDataManager.getCopiedList())

        adapterPinned.clear()
        adapterPinned.addAll(clipboardDataManager.getPinnedList())

        adapterCopied.notifyDataSetChanged()
        adapterPinned.notifyDataSetChanged()
    }

    fun startFloatingWidget(view: View) {
        val intent = Intent(this, FloatingWidgetService::class.java)
        startService(intent)
    }
}
