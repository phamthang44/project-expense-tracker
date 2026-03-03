package com.thang.projectexpensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thang.projectexpensetracker.data.AppDatabase
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Owns all expense-related concerns:
 *   1. Reads (expense list per project, single expense lookup, totals map).
 *   2. Writes (insert, update, delete).
 *
 * Expense form state, validation and draft lifecycle are handled
 * by [AddExpenseViewModel].
 * Project list, project form and sync are handled by their own focused ViewModels.
 */
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseDao = AppDatabase.getDatabase(application).expenseDao()

    // ── Real-time totals (used by AdminDashboardScreen budget bars) ────────
    val expenseTotalsMap: Flow<Map<Long, Double>> = expenseDao.getTotalsByProject()

    // ── Read operations ────────────────────────────────────────────────────

    fun getExpensesForProject(projectId: Long) = expenseDao.getExpensesByProject(projectId)

    fun getExpenseByIdFlow(expenseId: Long): Flow<ExpenseEntity?> = flow {
        emit(expenseDao.getExpenseById(expenseId))
    }

    // ── Write operations ───────────────────────────────────────────────────

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch { expenseDao.insertExpense(expense) }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { expenseDao.deleteExpense(expense) }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch { expenseDao.updateExpense(expense) }
    }
}
