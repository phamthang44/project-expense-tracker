package com.thang.projectexpensetracker.ui.screens

import java.text.NumberFormat
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Amount formatter used by ExpenseConfirmationScreen
// ─────────────────────────────────────────────────────────────────────────────
internal fun formatAmount(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)
