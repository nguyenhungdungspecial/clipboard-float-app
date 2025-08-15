package com.dung.clipboard

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.dung.clipboard.utils.Utils

class MainActivity : AppCompatActivity() {

    private lateinit var listCopied: ListView
    private lateinit var listPinned: ListView
    private lateinit var btnPin: Button
    private lateinit var btnUnpin: Button
    private lateinit var btnClear: Button

    private lateinit var copiedAdapter: ArrayAdapter<String>
    private lateinit var pinnedAdapter: ArrayAdapter<String>

    private var selectedCopied: String? = null
    private var selectedPinned: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listCopied = findViewById(R.id.list_copied)
        listPinned = findViewById(R.id.list_pinned)
        btnPin = findViewById(R.id.btn_pin)
        btnUnpin = findViewById(R.id.btn_unpin)
        btnClear = findViewById(R.id.btn_clear)

        // Lấy dữ liệu
        val copiedData: MutableList<String> = Utils.getCopiedList(this)
        val pinnedData: MutableList<String> = Utils.getPinnedList(this)

        // Adapter rõ ràng kiểu List<String> để tránh overload ambiguity
        copiedAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, copiedData)
        pinnedAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, pinnedData)

        listCopied.adapter = copiedAdapter
        listPinned.adapter = pinnedAdapter

        listCopied.setOnItemClickListener { _, _, position, _ ->
            selectedCopied = copiedAdapter.getItem(position)
        }

        listPinned.setOnItemClickListener { _, _, position, _ ->
            selectedPinned = pinnedAdapter.getItem(position)
        }

        btnPin.setOnClickListener {
            val value = selectedCopied ?: return@setOnClickListener
            Utils.pin(this, value)
            refreshPinned()
            Toast.makeText(this, "Đã ghim", Toast.LENGTH_SHORT).show()
        }

        btnUnpin.setOnClickListener {
            val value = selectedPinned ?: return@setOnClickListener
            Utils.unpin(this, value)
            refreshPinned()
            Toast.makeText(this, "Đã bỏ ghim", Toast.LENGTH_SHORT).show()
        }

        btnClear.setOnClickListener {
            Utils.clearCopied(this)
            refreshCopied()
            Toast.makeText(this, "Đã xóa danh sách đã sao chép", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshCopied() {
        copiedAdapter.clear()
        copiedAdapter.addAll(Utils.getCopiedList(this))
        copiedAdapter.notifyDataSetChanged()
        selectedCopied = null
    }

    private fun refreshPinned() {
        pinnedAdapter.clear()
        pinnedAdapter.addAll(Utils.getPinnedList(this))
        pinnedAdapter.notifyDataSetChanged()
        selectedPinned = null
    }
}
