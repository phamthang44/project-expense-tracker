package com.thang.projectexpensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thang.projectexpensetracker.data.AppDatabase
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import com.thang.projectexpensetracker.model.ProjectFormState
import com.thang.projectexpensetracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns exactly two concerns:
 *   1. Project form state (fields, validation, building the entity).
 *   2. Draft project lifecycle (hold → confirm → persist / update).
 *
 * Project list queries, expense operations and sync are handled
 * by their own focused ViewModels.
 */
class AddProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val projectDao = AppDatabase.getDatabase(application).projectDao()

    // ── Form State ─────────────────────────────────────────────────────────
    private val _projectFormState = MutableStateFlow(ProjectFormState())
    val projectFormState: StateFlow<ProjectFormState> = _projectFormState.asStateFlow()

    // ── Draft state (held between AddProjectScreen → ConfirmationScreen) ──
    private val _draftProject = MutableStateFlow<ProjectEntity?>(null)
    val draftProject: StateFlow<ProjectEntity?> = _draftProject.asStateFlow()

    private val _draftIsEditMode = MutableStateFlow(false)
    val draftIsEditMode: StateFlow<Boolean> = _draftIsEditMode.asStateFlow()

    // ── Form Operations ────────────────────────────────────────────────────

    fun updateProjectFormState(update: (ProjectFormState) -> ProjectFormState) {
        _projectFormState.update { update(it) }
    }

    /** Pre-fills the form when editing an existing project, or resets for a new one. */
    fun initProjectFormForEdit(project: ProjectEntity?) {
        _projectFormState.value = if (project != null) {
            ProjectFormState(
                projectName          = project.projectName,
                description          = project.description,
                manager              = project.manager,
                budgetStr            = project.budget.toString(),
                clientInfo           = project.clientInfo ?: "",
                specialRequirements  = project.specialRequirements ?: "",
                selectedStatus       = project.status,
                selectedPriority     = project.priority,
                startDateStr         = project.startDate,
                endDateStr           = project.endDate
            )
        } else {
            ProjectFormState()
        }
    }

    fun resetProjectForm() {
        _projectFormState.value = ProjectFormState()
    }

    /** Validates all fields; writes error messages into state and returns overall pass/fail. */
    fun validateProjectForm(): Boolean {
        val s = _projectFormState.value

        val nameErr    = if (s.projectName.isBlank()) "Project name is required" else null
        val descErr    = if (s.description.isBlank())  "Description is required"  else null
        val managerErr = if (s.manager.isBlank())      "Manager name is required" else null
        val startErr   = if (s.startDateStr.isBlank()) "Select a start date"      else null

        val budgetErr = when {
            s.budgetStr.isBlank()                -> "Budget is required"
            s.budgetStr.toDoubleOrNull() == null -> "Enter a valid number"
            s.budgetStr.toDouble() < 0           -> "Budget cannot be negative"
            else                                 -> null
        }

        val endErr = when {
            s.endDateStr.isBlank() -> "Select an end date"
            s.startDateStr.isNotBlank() &&
                DateUtils.parseDateToMillis(s.endDateStr)   != null &&
                DateUtils.parseDateToMillis(s.startDateStr) != null &&
                DateUtils.parseDateToMillis(s.endDateStr)!! <
                    DateUtils.parseDateToMillis(s.startDateStr)!! ->
                "End date cannot be before start date"
            else -> null
        }

        _projectFormState.update {
            it.copy(
                nameError    = nameErr,
                descError    = descErr,
                managerError = managerErr,
                budgetError  = budgetErr,
                startDateError = startErr,
                endDateError   = endErr
            )
        }
        return listOf(nameErr, descErr, managerErr, startErr, endErr, budgetErr).all { it == null }
    }

    /** Constructs a [ProjectEntity] from the current form state. */
    fun buildProjectFromForm(editProject: ProjectEntity?): ProjectEntity {
        val s    = _projectFormState.value
        val code = editProject?.projectCode ?: "PRJ-${System.currentTimeMillis() % 100000}"
        return ProjectEntity(
            id                  = editProject?.id ?: 0,
            projectCode         = code,
            projectName         = s.projectName.trim(),
            description         = s.description.trim(),
            startDate           = s.startDateStr,
            endDate             = s.endDateStr,
            manager             = s.manager.trim(),
            status              = s.selectedStatus,
            budget              = s.budgetStr.toDoubleOrNull() ?: 0.0,
            priority            = s.selectedPriority,
            specialRequirements = s.specialRequirements.trim().ifBlank { null },
            clientInfo          = s.clientInfo.trim().ifBlank { null }
        )
    }

    // ── Draft project lifecycle ────────────────────────────────────────────

    fun setDraftProject(project: ProjectEntity, isEditMode: Boolean = false) {
        _draftProject.value    = project
        _draftIsEditMode.value = isEditMode
    }

    fun saveDraftToDb() {
        viewModelScope.launch {
            _draftProject.value?.let {
                projectDao.insertProject(it)
                _draftProject.value    = null
                _draftIsEditMode.value = false
            }
        }
    }

    fun updateDraftInDb() {
        viewModelScope.launch {
            _draftProject.value?.let {
                projectDao.updateProject(it)
                _draftProject.value    = null
                _draftIsEditMode.value = false
            }
        }
    }
}
