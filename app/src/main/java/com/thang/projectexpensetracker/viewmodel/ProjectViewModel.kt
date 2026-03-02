package com.thang.projectexpensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thang.projectexpensetracker.data.AppDatabase
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

enum class SyncStatus { IDLE, SYNCING, SUCCESS, ERROR, OFFLINE }

class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val projectDao = db.projectDao()
    private val expenseDao = db.expenseDao()

    // ── Project list state ─────────────────────────────────────────
    private val _allProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val allProjects: StateFlow<List<ProjectEntity>> = _allProjects.asStateFlow()

    // ── Draft state (for Confirmation screen) ─────────────────────
    private val _draftProject = MutableStateFlow<ProjectEntity?>(null)
    val draftProject: StateFlow<ProjectEntity?> = _draftProject.asStateFlow()

    private val _draftIsEditMode = MutableStateFlow(false)
    val draftIsEditMode: StateFlow<Boolean> = _draftIsEditMode.asStateFlow()

    // ── Draft expense state (for Add/Edit Expense confirmation) ────
    private val _draftExpense = MutableStateFlow<ExpenseEntity?>(null)
    val draftExpense: StateFlow<ExpenseEntity?> = _draftExpense.asStateFlow()

    private val _draftIsEditModeExpense = MutableStateFlow(false)
    val draftIsEditModeExpense: StateFlow<Boolean> = _draftIsEditModeExpense.asStateFlow()

    // ── Real-time expense totals per project (for dashboard budget bars) ───
    val expenseTotalsMap: kotlinx.coroutines.flow.Flow<Map<Long, Double>> =
        expenseDao.getTotalsByProject()

    // ── Sync state ─────────────────────────────────────────────────
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // ── Pending items count (unsynced) ─────────────────────────────
    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    // ── Advanced search results ────────────────────────────────────
    private val _advancedSearchResults = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val advancedSearchResults: StateFlow<List<ProjectEntity>> = _advancedSearchResults.asStateFlow()

    private val _isAdvancedSearchActive = MutableStateFlow(false)
    val isAdvancedSearchActive: StateFlow<Boolean> = _isAdvancedSearchActive.asStateFlow()

    init {
        loadAllProjects()
    }

    private fun loadAllProjects() {
        viewModelScope.launch {
            projectDao.getAllProjects().collectLatest {
                _allProjects.value = it
                _pendingSyncCount.value = it.size // In a real app, track unsynced items
            }
        }
    }

    // ── Search ─────────────────────────────────────────────────────
    fun searchProjects(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                projectDao.getAllProjects().collectLatest { _allProjects.value = it }
            } else {
                val searchQuery = "%$query%"
                projectDao.searchProjects(searchQuery).collectLatest { _allProjects.value = it }
            }
        }
    }

    // ── Advanced Search ────────────────────────────────────────────
    /**
     * Runs an advanced search with optional filters.
     * Dates are passed in dd/MM/yyyy display format and converted to yyyy-MM-dd
     * for correct lexicographic ordering in the Room query.
     */
    fun advancedSearch(
        query: String      = "",
        status: String?    = null,
        owner: String?     = null,
        startAfter: String? = null,   // dd/MM/yyyy
        endBefore: String?  = null    // dd/MM/yyyy
    ) {
        _isAdvancedSearchActive.value = true
        viewModelScope.launch {
            val textQuery   = if (query.isBlank()) "%" else "%$query%"
            val statusParam = status?.takeIf { it.isNotBlank() }
            val ownerParam  = owner?.takeIf  { it.isNotBlank() }?.let { "%$it%" }
            val startParam  = startAfter?.takeIf { it.isNotBlank() }?.let { toIso(it) }
            val endParam    = endBefore?.takeIf  { it.isNotBlank() }?.let { toIso(it) }

            projectDao.advancedSearch(textQuery, statusParam, ownerParam, startParam, endParam)
                .collectLatest { _advancedSearchResults.value = it }
        }
    }

    fun clearAdvancedSearch() {
        _isAdvancedSearchActive.value = false
        _advancedSearchResults.value = emptyList()
    }

    /** Converts dd/MM/yyyy → yyyy-MM-dd for lexicographic date comparisons in SQL. */
    private fun toIso(displayDate: String): String? = try {
        val display = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val iso     = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        iso.format(display.parse(displayDate)!!)
    } catch (_: Exception) { null }

    // ── Draft project (Confirmation flow) ─────────────────────────
    fun setDraftProject(project: ProjectEntity, isEditMode: Boolean = false) {
        _draftProject.value = project
        _draftIsEditMode.value = isEditMode
    }

    fun saveDraftToDb() {
        viewModelScope.launch {
            _draftProject.value?.let {
                projectDao.insertProject(it)
                _draftProject.value = null
                _draftIsEditMode.value = false
            }
        }
    }

    fun updateDraftInDb() {
        viewModelScope.launch {
            _draftProject.value?.let {
                projectDao.updateProject(it)
                _draftProject.value = null
                _draftIsEditMode.value = false
            }
        }
    }

    // ── Project CRUD ───────────────────────────────────────────────
    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectDao.deleteProject(project)
        }
    }

    // ── Expense operations ─────────────────────────────────────────
    fun getExpensesForProject(projectId: Long) = expenseDao.getExpensesByProject(projectId)

    fun getExpenseByIdFlow(expenseId: Long) = flow<ExpenseEntity?> {
        emit(expenseDao.getExpenseById(expenseId))
    }

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseDao.insertExpense(expense)
        }
    }

    fun setDraftExpense(expense: ExpenseEntity, isEditMode: Boolean = false) {
        _draftExpense.value = expense
        _draftIsEditModeExpense.value = isEditMode
    }

    fun saveDraftExpense() {
        viewModelScope.launch {
            _draftExpense.value?.let {
                if (_draftIsEditModeExpense.value) {
                    expenseDao.updateExpense(it)
                } else {
                    expenseDao.insertExpense(it)
                }
                _draftExpense.value = null
                _draftIsEditModeExpense.value = false
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseDao.deleteExpense(expense)
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectDao.updateProject(project)
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseDao.updateExpense(expense)
        }
    }

    // ── Cloud Sync simulation ──────────────────────────────────────
    fun triggerSync() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            delay(2500) // Simulate network call
            // 80% success rate simulation
            val success = (1..10).random() <= 8
            if (success) {
                _syncStatus.value = SyncStatus.SUCCESS
                val now = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date())
                _lastSyncTime.value = now
                _pendingSyncCount.value = 0
                delay(3000)
                _syncStatus.value = SyncStatus.IDLE
            } else {
                _syncStatus.value = SyncStatus.ERROR
            }
        }
    }

    fun setOfflineMode(offline: Boolean) {
        _isOffline.value = offline
        if (offline) _syncStatus.value = SyncStatus.OFFLINE
        else _syncStatus.value = SyncStatus.IDLE
    }

    fun dismissSyncError() {
        _syncStatus.value = SyncStatus.IDLE
    }
}