package com.thang.projectexpensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thang.projectexpensetracker.data.AppDatabase
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.model.ExpenseFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns exactly two concerns:
 *   1. Expense form state (fields, validation, building the entity).
 *   2. Draft expense lifecycle (hold → confirm → persist / update).
 *
 * Expense list queries and CRUD writes are handled by [ExpenseViewModel].
 */
class AddExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseDao = AppDatabase.getDatabase(application).expenseDao()

    // ── Form State ─────────────────────────────────────────────────────────
    private val _expenseFormState = MutableStateFlow(ExpenseFormState())
    val expenseFormState: StateFlow<ExpenseFormState> = _expenseFormState.asStateFlow()

    // ── Draft state (held between AddExpenseScreen → ExpenseConfirmationScreen) ──
    private val _draftExpense = MutableStateFlow<ExpenseEntity?>(null)
    val draftExpense: StateFlow<ExpenseEntity?> = _draftExpense.asStateFlow()

    private val _draftIsEditMode = MutableStateFlow(false)
    val draftIsEditMode: StateFlow<Boolean> = _draftIsEditMode.asStateFlow()

    // ── Form Operations ────────────────────────────────────────────────────

    fun updateExpenseFormState(update: (ExpenseFormState) -> ExpenseFormState) {
        _expenseFormState.update { update(it) }
    }

    /** Pre-fills the form when editing an existing expense, or resets for a new one. */
    fun initExpenseFormForEdit(expense: ExpenseEntity?) {
        _expenseFormState.value = if (expense != null) {
            ExpenseFormState(
                dateStr         = expense.date,
                amountStr       = expense.amount.toString(),
                currency        = expense.currency,
                claimant        = expense.claimant,
                description     = expense.description ?: "",
                location        = expense.location ?: "",
                selectedPayment = expense.paymentMethod,
                selectedType    = expense.type,
                selectedStatus  = expense.paymentStatus
            )
        } else {
            ExpenseFormState()
        }
    }

    fun resetExpenseForm() {
        _expenseFormState.value = ExpenseFormState()
    }

    /** Validates all fields; writes error messages into state and returns overall pass/fail. */
    fun validateExpenseForm(): Boolean {
        val s = _expenseFormState.value

        val dateErr    = if (s.dateStr.isBlank())      "Please select a date"     else null
        val claimantErr = if (s.claimant.isBlank())    "Required"                  else null
        val typeErr    = if (s.selectedType.isBlank()) "Please select a category" else null

        val amountErr = when {
            s.amountStr.isBlank()                -> "Required"
            s.amountStr.toDoubleOrNull() == null -> "Invalid number"
            s.amountStr.toDouble() <= 0          -> "Must be > 0"
            else                                 -> null
        }

        _expenseFormState.update {
            it.copy(
                dateError     = dateErr,
                amountError   = amountErr,
                claimantError = claimantErr,
                typeError     = typeErr
            )
        }
        return listOf(dateErr, amountErr, claimantErr, typeErr).all { it == null }
    }

    /** Constructs an [ExpenseEntity] from the current form state. */
    fun buildExpenseFromForm(editExpense: ExpenseEntity?, projectId: Long): ExpenseEntity {
        val s = _expenseFormState.value
        return ExpenseEntity(
            expenseId     = editExpense?.expenseId ?: 0,
            projectId     = projectId,
            date          = s.dateStr,
            amount        = s.amountStr.toDoubleOrNull() ?: 0.0,
            currency      = s.currency.substringBefore(" ").trim(),
            type          = s.selectedType,
            paymentMethod = s.selectedPayment,
            claimant      = s.claimant.trim(),
            paymentStatus = s.selectedStatus,
            description   = s.description.trim().ifBlank { null },
            location      = s.location.trim().ifBlank { null }
        )
    }

    // ── Draft expense lifecycle ────────────────────────────────────────────

    fun setDraftExpense(expense: ExpenseEntity, isEditMode: Boolean = false) {
        _draftExpense.value    = expense
        _draftIsEditMode.value = isEditMode
    }

    fun saveDraftExpense() {
        viewModelScope.launch {
            _draftExpense.value?.let {
                if (_draftIsEditMode.value) expenseDao.updateExpense(it)
                else                        expenseDao.insertExpense(it)
                _draftExpense.value    = null
                _draftIsEditMode.value = false
            }
        }
    }
}
