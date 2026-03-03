package com.thang.projectexpensetracker.model

/**
 * Holds all mutable field values and their validation error messages
 * for the Add / Edit Project form.
 *
 * Single Responsibility: pure data carrier for the project form layer —
 * no behaviour, no database knowledge, no UI dependency.
 */
data class ProjectFormState(
    // ── Input fields ──────────────────────────────────────────────
    val projectName: String = "",
    val description: String = "",
    val manager: String = "",
    val budgetStr: String = "",
    val clientInfo: String = "",
    val specialRequirements: String = "",
    val selectedStatus: String = "Active",
    val selectedPriority: String = "Normal",
    val startDateStr: String = "",
    val endDateStr: String = "",

    // ── Validation error messages (null = no error) ───────────────
    val nameError: String? = null,
    val descError: String? = null,
    val managerError: String? = null,
    val budgetError: String? = null,
    val startDateError: String? = null,
    val endDateError: String? = null
)
