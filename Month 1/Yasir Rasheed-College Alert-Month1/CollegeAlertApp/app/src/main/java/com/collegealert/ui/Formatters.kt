package com.collegealert.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFmt = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
private val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())

fun formatDate(millis: Long): String = dateFmt.format(Date(millis))
fun formatTime(millis: Long): String = timeFmt.format(Date(millis))

/** Human friendly countdown such as "in 2 days" or "3 hours ago". */
fun countdown(millis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = millis - now
    val abs = kotlin.math.abs(diff)
    val minutes = abs / 60_000
    val text = when {
        minutes < 1 -> "now"
        minutes < 60 -> "$minutes min"
        minutes < 60 * 24 -> "${minutes / 60} h"
        else -> "${minutes / (60 * 24)} d"
    }
    return when {
        text == "now" -> "Starting now"
        diff > 0 -> "in $text"
        else -> "$text ago"
    }
}

val reminderOptions = listOf(
    -1 to "None",
    0 to "At start",
    15 to "15 min",
    30 to "30 min",
    60 to "1 hour",
    1440 to "1 day"
)

fun reminderLabel(minutes: Int): String =
    reminderOptions.firstOrNull { it.first == minutes }?.second ?: "$minutes min"
