package com.dung.clipboard

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.ListView
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.util.Log

class FloatingWidgetService : Service() {

    companion object {
        @Volatile
        var isRunning = false
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private lateinit var lvCopied: ListView
    private lateinit var lvPinned: ListView

    private val clipboardUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("FloatingWidgetService", "Received clipboard update broadcast.")
            refreshLists()
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addFloatingWidgetContent()
        refreshLists()

        // Đăng ký receiver với chuỗi hành động đã sửa đổi
        val filter = IntentFilter("com.dung.clipboard.CLIPBOARD_UPDATED")
        registerReceiver(clipboardUpdateReceiver, filter)
    }

    private fun addFloatingWidgetContent() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_widget_content_layout, null)
        lvCopied = floatingView!!.findViewById(R.id.lvCopied)
        lvPinned = floatingView!!.findViewById(R.id.lvPinned)

        val btnClose = floatingView!!.findViewById<Button>(R.id.btnClose)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params!!.gravity = Gravity.CENTER

        windowManager!!.addView(floatingView, params)

        btnClose.setOnClickListener {
            stopSelf()
        }

        lvCopied.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val text = parent.getItemAtPosition(position).toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("clipboard_item", text))
            stopSelf()
        }

        lvPinned.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val text = parent.getItemAtPosition(position).toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("clipboard_item", text))
            stopSelf()
        }
    }

    private fun refreshLists() {
        val copiedList = ClipboardDataManager.getCopiedList(this)
        val copiedAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, copiedList)
        lvCopied.adapter = copiedAdapter

        val pinnedList = ClipboardDataManager.getPinnedList(this)
        val pinnedAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, pinnedList)
        lvPinned.adapter = pinnedAdapter
    }

    override fun onDestroy() {
        isRunning = false
        if (floatingView != null) windowManager?.removeView(floatingView)
        unregisterReceiver(clipboardUpdateReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

