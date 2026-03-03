package com.thang.projectexpensetracker.infrastructure

import com.thang.projectexpensetracker.data.dao.ExpenseDao
import com.thang.projectexpensetracker.data.dao.ProjectDao
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Bridge between Room (local) and Firestore (cloud).
 * Handles uploading local data and observing changes for auto-sync.
 */
class ProjectRepository(
    private val projectDao: ProjectDao,
    private val expenseDao: ExpenseDao
) {

    /** Upload all local projects and expenses to Firestore. Returns true if both succeed. */
    suspend fun syncAll(): Boolean {
        val projects = projectDao.getAllProjectsList()
        val expenses = expenseDao.getAllExpenses()
        val projOk = FirebaseHelper.uploadProjects(projects)
        val expOk  = FirebaseHelper.uploadExpenses(expenses)
        return projOk && expOk
    }

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