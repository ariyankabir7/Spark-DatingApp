package com.ariyan.spark.utils

import android.util.Log

object AppLogger {

    private const val DEFAULT_TAG = "SparkApp"

    /**
     * Logs a debug message by default when AppLogger is invoked as a function.
     * e.g., AppLogger("This is a debug message")
     */
    operator fun invoke(message: String, tag: String = DEFAULT_TAG) {
        d(message, tag) // Calls the existing d function
    }

    fun v(message: String, tag: String = DEFAULT_TAG) {
        Log.v(tag, message)
    }

    fun d(message: String, tag: String = DEFAULT_TAG) {
        Log.d(tag, message)
    }

    fun i(message: String, tag: String = DEFAULT_TAG) {
        Log.i(tag, message)
    }

    fun w(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }

    fun e(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}
