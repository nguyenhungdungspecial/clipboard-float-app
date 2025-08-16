package com.dung.clipboard

import java.util.concurrent.atomic.AtomicBoolean

object ActivityVisibility {
    private val visible = AtomicBoolean(false)
    fun onResume() { visible.set(true) }
    fun onPause() { visible.set(false) }
    fun isVisible(): Boolean = visible.get()
}
