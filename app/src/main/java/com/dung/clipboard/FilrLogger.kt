package com.dung.clipboard

import android.util.Log

object FileLogger {
    fun d(tag: String, msg: String) = Log.d(tag, msg)
    fun e(tag: String, msg: String, t: Throwable? = null) = Log.e(tag, msg, t)
}
