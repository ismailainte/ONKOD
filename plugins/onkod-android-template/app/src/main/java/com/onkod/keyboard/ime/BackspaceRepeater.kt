package com.onkod.keyboard.ime

import android.os.Handler
import android.os.Looper

class BackspaceRepeater(private val onDelete: () -> Unit) {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private var repeatCount = 0

    private val repeatRunnable = object : Runnable {
        override fun run() {
            if (!running) return
            onDelete()
            repeatCount += 1
            handler.postDelayed(this, nextDelay())
        }
    }

    fun start(delay: LongPressDelay) {
        stop()
        running = true
        repeatCount = 0
        handler.postDelayed(repeatRunnable, initialDelay(delay))
    }

    fun stop() {
        running = false
        handler.removeCallbacks(repeatRunnable)
    }

    private fun initialDelay(delay: LongPressDelay): Long = when (delay) {
        LongPressDelay.SHORT -> 260L
        LongPressDelay.NORMAL -> 420L
        LongPressDelay.LONG -> 620L
    }

    private fun nextDelay(): Long = when {
        repeatCount > 24 -> 45L
        repeatCount > 10 -> 70L
        else -> 95L
    }
}
