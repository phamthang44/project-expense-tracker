package com.thang.projectexpensetracker.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Pure number / currency formatting utilities shared across the app.
 *
 * Single Responsibility: string / number formatting only —
 * no ViewModel state, no UI composable dependency.
 */
object FormatUtils {

    /**
     * Formats a budget/expense amount as an integer-precision number string,
     * e.g. 12500.0 → "12,500".
     */
    fun formatBudget(amount: Double): String =
        NumberFormat.getNumberInstance(Locale.US)
            .apply { minimumFractionDigits = 0; maximumFractionDigits = 0 }
            .format(amount)
}
