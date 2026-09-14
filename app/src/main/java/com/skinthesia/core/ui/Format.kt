package com.skinthesia.core.ui

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val IndianEnglish: Locale = Locale.Builder().setLanguage("en").setRegion("IN").build()
private val RupeeFormat: NumberFormat = NumberFormat.getIntegerInstance(IndianEnglish)
private val DayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
private val WeekdayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.getDefault())
private val ShortDayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
private val DateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a", Locale.getDefault())

/** Whole rupees with Indian digit grouping, for example ₹1,250. */
fun formatPrice(rupees: Int): String = "₹" + synchronized(RupeeFormat) { RupeeFormat.format(rupees) }

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun LocalDate.formatDay(): String = format(DayFormat)

fun LocalDate.formatWeekday(): String = format(WeekdayFormat)

fun LocalDate.formatShort(): String = format(ShortDayFormat)

fun formatDay(epochMillis: Long): String = epochMillis.toLocalDate().formatDay()

fun formatTime(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(TimeFormat)

fun formatDateTime(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(DateTimeFormat)

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
