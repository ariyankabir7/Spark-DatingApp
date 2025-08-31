package com.ariyan.spark.utils

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {

    /**
     * Converts a timestamp (in seconds) into a user-friendly presence string.
     * e.g., "Online", "Active 5m ago", "Active yesterday"
     */
    fun getPresenceTimestamp(timeInMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeInMillis

        // If the difference is less than 2 minutes (120,000 milliseconds),
        // consider the user to be "Online".
        return if (diff < 120_000) {
            "Online"
        } else {
            // Otherwise, format the timestamp to show the time.
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timeInMillis))
        }
    }

}
