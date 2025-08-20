package com.dung.clipboard

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ListView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ArrayAdapter

class FloatingContentService : Service() {

    companion object {
        @Volatile
        var isRunning = false
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private lateinit var lvCopied: ListView
    private lateinit var lvPinned: ListView

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addFloatingWidgetContent()
        refreshLists()
    }

    private fun addFloatingWidgetContent() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_widget_content_layout, null)
        lvCopied = floatingView!!.findViewById(R.id.lvCopied)
        lvPinned = floatingView!!.findViewById(R.id.lvPinned)

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
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

