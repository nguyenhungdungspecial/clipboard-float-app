package com.dung.clipboard

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileLogger(private val context: Context) {
    private val LOG_FILE_NAME = "app_log.txt"

    fun log(tag: String, message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "$timestamp [$tag] $message\n"

        try {
            val logFile = File(context.filesDir, LOG_FILE_NAME)
            FileWriter(logFile, true).use { writer ->
                writer.append(logMessage)
            }
        } catch (e: Exception) {
            // Trong trường hợp không ghi được log, chúng ta sẽ bỏ qua để ứng dụng không crash
            // và log lỗi ra một chỗ khác nếu có thể (nhưng với yêu cầu hiện tại thì không cần thiết)
        }
    }
    
    fun getLogs(): String {
        val logFile = File(context.filesDir, LOG_FILE_NAME)
        return try {
            if (logFile.exists()) {
                logFile.readText()
            } else {
                "Không tìm thấy file log."
            }
        } catch (e: Exception) {
            "Lỗi khi đọc file log: ${e.message}"
        }
    }

    fun clearLogs() {
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

