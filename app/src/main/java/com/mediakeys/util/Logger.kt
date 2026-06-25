package com.mediakeys.util

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Global logging utility that buffers the last [MAX_LOGS] lines in memory
 * for display in the in-app debug console.
 */
object Logger {
    private const val TAG = "MediaKeys"
    private const val MAX_LOGS = 50
    
    private val logBuffer = ConcurrentLinkedQueue<String>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    var onLogUpdate: (() -> Unit)? = null

    fun d(message: String) {
        Log.d(TAG, message)
        append("DEBUG", message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
        append("ERROR", "$message ${throwable?.message ?: ""}")
    }

    fun i(message: String) {
        Log.i(TAG, message)
        append("INFO", message)
    }

    private fun append(level: String, message: String) {
        val time = timeFormat.format(Date())
        val entry = "[$time] $level: $message"
        
        logBuffer.add(entry)
        while (logBuffer.size > MAX_LOGS) {
            logBuffer.poll()
        }
        onLogUpdate?.invoke()
    }

    fun getLogs(): String = logBuffer.joinToString("\n")

    fun clear() {
        logBuffer.clear()
        onLogUpdate?.invoke()
    }
}
