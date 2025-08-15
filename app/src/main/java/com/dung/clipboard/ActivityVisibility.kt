package com.dung.clipboard

/**
 * Cờ theo dõi Activity có đang hiển thị hay không để FloatingWidget toggle mở/đóng.
 */
object ActivityVisibility {
    @Volatile var visible: Boolean = false
}
