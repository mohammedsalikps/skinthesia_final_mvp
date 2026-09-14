package com.skinthesia.core.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun pattern(pattern: String): DateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())

/** Whole rupees with Indian digit grouping (₹1,250 and ₹1,25,000), independent of device locale data. */
fun formatPrice(rupees: Int): String {
    val digits = kotlin.math.abs(rupees.toLong()).toString()
    val grouped = if (digits.length <= 3) {
        digits
    } else {
        digits.dropLast(3).reversed().chunked(2).joinToString(",").reversed() + "," + digits.takeLast(3)
    }
    return (if (rupees < 0) "-₹" else "₹") + grouped
}

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun LocalDate.formatDay(): String = format(pattern("d MMM yyyy"))

fun LocalDate.formatWeekday(): String = format(pattern("EEEE, d MMM"))

fun LocalDate.formatShort(): String = format(pattern("d MMM"))

fun formatDay(epochMillis: Long): String = epochMillis.toLocalDate().formatDay()

fun formatTime(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(pattern("h:mm a"))

fun formatDateTime(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(pattern("EEE, d MMM · h:mm a"))

/** "Just now", "5 min ago", "3 h ago", "2 d ago", then a date. */
fun relativeTime(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = (now - epochMillis) / 60_000
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 60 * 24 -> "${minutes / 60} h ago"
        minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)} d ago"
        else -> formatDay(epochMillis)
    }
}
