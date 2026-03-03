package com.thang.projectexpensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thang.projectexpensetracker.data.AppDatabase
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import com.thang.projectexpensetracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Owns project-list concerns only:
 *   1. Loading and exposing the live list of all projects.
 *   2. Simple keyword search.
 *   3. Advanced multi-filter search.
 *   4. Project delete / update (structural CRUD without the form layer).
 *
 * Form state, draft lifecycle, expense operations and sync are each
 * handled by their own focused ViewModels.
 */
class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val projectDao = AppDatabase.getDatabase(application).projectDao()

    // ── Project list ───────────────────────────────────────────────────────
    private val _allProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val allProjects: StateFlow<List<ProjectEntity>> = _allProjects.asStateFlow()

    // ── Advanced search results ────────────────────────────────────────────
    private val _advancedSearchResults = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val advancedSearchResults: StateFlow<List<ProjectEntity>> = _advancedSearchResults.asStateFlow()

    private val _isAdvancedSearchActive = MutableStateFlow(false)
    val isAdvancedSearchActive: StateFlow<Boolean> = _isAdvancedSearchActive.asStateFlow()

    init {
        loadAllProjects()
    }

    private fun loadAllProjects() {
        viewModelScope.launch {
            projectDao.getAllProjects().collectLatest { _allProjects.value = it }
        }
    }

    // ── Search ─────────────────────────────────────────────────────────────
    fun searchProjects(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                projectDao.getAllProjects().collectLatest { _allProjects.value = it }
            } else {
                projectDao.searchProjects("%$query%").collectLatest { _allProjects.value = it }
            }
        }
    }

    /**
     * Multi-filter search. Dates are passed as "dd/MM/yyyy" and converted to
     * "yyyy-MM-dd" internally for lexicographic Room queries.
     */
    fun advancedSearch(
        query: String       = "",
        status: String?     = null,
        owner: String?      = null,
        startAfter: String? = null,
        endBefore: String?  = null
    ) {
        _isAdvancedSearchActive.value = true
        viewModelScope.launch {
            val textQuery   = if (query.isBlank()) "%" else "%$query%"
            val statusParam = status?.takeIf { it.isNotBlank() }
            val ownerParam  = owner?.takeIf  { it.isNotBlank() }?.let { "%$it%" }
            val startParam  = startAfter?.takeIf { it.isNotBlank() }?.let { DateUtils.toIsoDate(it) }
            val endParam    = endBefore?.takeIf  { it.isNotBlank() }?.let { DateUtils.toIsoDate(it) }

            projectDao.advancedSearch(textQuery, statusParam, ownerParam, startParam, endParam)
                .collectLatest { _advancedSearchResults.value = it }
        }
    }

    fun clearAdvancedSearch() {
        _isAdvancedSearchActive.value = false
        _advancedSearchResults.value  = emptyList()
    }

    // ── CRUD ───────────────────────────────────────────────────────────────
    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch { projectDao.deleteProject(project) }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch { projectDao.updateProject(project) }
    }
}