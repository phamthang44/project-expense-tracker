package com.thang.projectexpensetracker.infrastructure

import com.thang.projectexpensetracker.data.dao.ExpenseDao
import com.thang.projectexpensetracker.data.dao.ProjectDao
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Bridge between Room (local) and Firestore (cloud).
 * Provides both full-sync and individual CRUD mirror operations.
 */
class ProjectRepository(
    private val projectDao: ProjectDao,
    private val expenseDao: ExpenseDao
) {

    // ── Full sync (manual "Sync Now" + auto-sync) ──────────────────────────

    /** Upload all local projects and expenses to Firestore. Returns true if both succeed. */
    suspend fun syncAll(): Boolean {
        val projects = projectDao.getAllProjectsList()
        val expenses = expenseDao.getAllExpenses()
        val projOk = FirebaseHelper.uploadProjects(projects)
        val expOk  = FirebaseHelper.uploadExpenses(expenses)
        return projOk && expOk
    }

    /**
     * Full bidirectional sync: upload local changes AND download cloud changes.
     * Cloud data takes precedence when conflicts occur.
     * Returns true if both upload and download succeed.
     */
    suspend fun syncBidirectional(): Boolean {
        // Upload phase: push local changes to cloud
        val projects = projectDao.getAllProjectsList()
        val expenses = expenseDao.getAllExpenses()
        val uploadOk = FirebaseHelper.uploadProjects(projects) && 
                       FirebaseHelper.uploadExpenses(expenses)
        
        // Download phase: pull cloud changes into local
        val downloadOk = syncDownloadExpenses()
        
        return uploadOk && downloadOk
    }

    /**
     * Download all expenses from Firestore and merge into local database.
     * Cloud data overwrites local data for matching expense IDs.
     * Returns true if download succeeds (even if zero expenses exist).
     */
    suspend fun syncDownloadExpenses(): Boolean {
        return try {
            val cloudExpenses = FirebaseHelper.downloadExpenses()
            if (cloudExpenses.isNotEmpty()) {
                // Upsert all expenses: update if exists, insert if new
                cloudExpenses.forEach { expense ->
                    val existing = expenseDao.getExpenseById(expense.expenseId)
                    if (existing != null) {
                        expenseDao.updateExpense(expense)
                    } else {
                        expenseDao.insertExpense(expense)
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Download expenses for a specific project from Firestore and merge into local.
     * Cloud data overwrites local data for matching expense IDs.
     */
    suspend fun syncDownloadExpensesForProject(projectId: Long): Boolean {
        return try {
            val cloudExpenses = FirebaseHelper.downloadExpensesByProject(projectId)
            if (cloudExpenses.isNotEmpty()) {
                cloudExpenses.forEach { expense ->
                    val existing = expenseDao.getExpenseById(expense.expenseId)
                    if (existing != null) {
                        expenseDao.updateExpense(expense)
                    } else {
                        expenseDao.insertExpense(expense)
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ── Individual project CRUD → Firestore ────────────────────────────────

    /** Mirror a newly-inserted or updated project to Firestore. */
    suspend fun syncUpsertProject(project: ProjectEntity): Boolean =
        FirebaseHelper.upsertProject(project)

    /** Mirror a project deletion to Firestore (also removes its expenses). */
    suspend fun syncDeleteProject(project: ProjectEntity): Boolean {
        val projOk = FirebaseHelper.deleteProject(project.projectCode)
        val expOk  = FirebaseHelper.deleteExpensesByProject(project.id)
        return projOk && expOk
    }

    // ── Individual expense CRUD → Firestore ────────────────────────────────

    /** Mirror a newly-inserted or updated expense to Firestore. */
    suspend fun syncUpsertExpense(expense: ExpenseEntity): Boolean =
        FirebaseHelper.upsertExpense(expense)

    /** Mirror an expense deletion to Firestore. */
    suspend fun syncDeleteExpense(expense: ExpenseEntity): Boolean =
        FirebaseHelper.deleteExpense(expense.projectId, expense.expenseId)

    // ── Observables (auto-sync & pending count) ────────────────────────────

    /** Observable stream that emits whenever any local data changes (for auto-sync). */
    fun observeAllData(): Flow<Pair<List<ProjectEntity>, List<ExpenseEntity>>> =
        combine(
            projectDao.getAllProjects(),
            expenseDao.getAllExpensesFlow()
        ) { projects, expenses -> Pair(projects, expenses) }

    /** Total count of local items (projects + expenses). */
    fun getPendingCount(): Flow<Int> =
        combine(
            projectDao.getAllProjects(),
            expenseDao.getAllExpensesFlow()
        ) { projects, expenses -> projects.size + expenses.size }
}