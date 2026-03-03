package com.thang.projectexpensetracker.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Pure date-formatting / parsing utilities shared across the app.
 *
 * Single Responsibility: date string conversions only —
 * no ViewModel state, no UI composable dependency.
 */
object DateUtils {

    /** Converts epoch milliseconds to a display string ("dd/MM/yyyy"), UTC-safe. */
    fun millisToDisplayDate(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .also { it.timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(millis))

    /** Converts epoch milliseconds to a search-friendly string ("MMM dd, yyyy"), UTC-safe. */
    fun millisToSearchDisplayDate(millis: Long): String =
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .also { it.timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(millis))

    /** Parses a "dd/MM/yyyy" display string to epoch milliseconds, or null on failure. */
    fun parseDateToMillis(dateStr: String): Long? = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .also { it.timeZone = TimeZone.getTimeZone("UTC") }
            .parse(dateStr)?.time
    } catch (_: Exception) { null }

    /**
     * Converts a "dd/MM/yyyy" display date to "yyyy-MM-dd" ISO format, required for
     * lexicographic date comparisons in Room/SQL queries. Returns null on failure.
     */
    fun toIsoDate(displayDate: String): String? = try {
        val display = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val iso     = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        iso.format(display.parse(displayDate)!!)
    } catch (_: Exception) { null }
}
