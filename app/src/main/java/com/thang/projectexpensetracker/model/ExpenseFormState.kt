package com.thang.projectexpensetracker.model

/**
 * Holds all mutable field values and their validation error messages
 * for the Add / Edit Expense form.
 *
 * Single Responsibility: pure data carrier for the expense form layer —
 * no behaviour, no database knowledge, no UI dependency.
 */
data class ExpenseFormState(
    // ── Input fields ──────────────────────────────────────────────
    val dateStr: String = "",
    val amountStr: String = "",
    val currency: String = "USD",
    val claimant: String = "",
    val description: String = "",
    val location: String = "",
    val isLocating: Boolean = false,
    val selectedPayment: String = "Cash",
    val selectedType: String = "",
    val selectedStatus: String = "Pending",

    // ── Validation error messages (null = no error) ───────────────
    val dateError: String? = null,
    val amountError: String? = null,
    val claimantError: String? = null,
    val typeError: String? = null
)
