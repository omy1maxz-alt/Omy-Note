package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormats {
    fun formatNoteTimestamp(epochMillis: Long, customPattern: String = "dd/MM/yyyy"): String {
        val now = Calendar.getInstance()
        val noteDate = Calendar.getInstance().apply { timeInMillis = epochMillis }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = timeFormat.format(Date(epochMillis))

        val isToday = now.get(Calendar.YEAR) == noteDate.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == noteDate.get(Calendar.DAY_OF_YEAR)

        if (isToday) {
            return "Today, $timeString"
        }

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == noteDate.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == noteDate.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) {
            return "Yesterday, $timeString"
        }

        return try {
            val dateFormat = SimpleDateFormat(customPattern, Locale.getDefault())
            "${dateFormat.format(Date(epochMillis))}, $timeString"
        } catch (e: Exception) {
            val fallback = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            "${fallback.format(Date(epochMillis))}, $timeString"
        }
    }
}
