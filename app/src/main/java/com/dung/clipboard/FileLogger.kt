package com.dung.clipboard

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private lateinit var context: Context
    private const val LOG_FILE_NAME = "app_log.txt"

    fun initialize(context: Context) {
        this.context = context
    }

    fun log(tag: String, message: String) {
        if (!::context.isInitialized) return

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "$timestamp [$tag] $message\n"

        try {
            val logFile = File(context.filesDir, LOG_FILE_NAME)
            FileWriter(logFile, true).use { writer ->
                writer.append(logMessage)
            }
        } catch (e: Exception) {
            // Không thể ghi log vào file, có thể do lỗi quyền hạn hoặc IO
            // Trong trường hợp này, không thể làm gì thêm, chỉ có thể bỏ qua
        }
    }
    
    fun clearLogs() {
        if (!::context.isInitialized) return
        try {
            val logFile = File(context.filesDir, LOG_FILE_NAME)
            if (logFile.exists()) {
                logFile.delete()
            }
        } catch (e: Exception) {
            // Bỏ qua lỗi
        }
    }
}

