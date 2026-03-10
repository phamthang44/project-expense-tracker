package com.thang.projectexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thang.projectexpensetracker.ui.screens.*
import com.thang.projectexpensetracker.ui.theme.ProjectExpenseTrackerTheme
import com.thang.projectexpensetracker.infrastructure.ThemePreferences
import com.thang.projectexpensetracker.viewmodel.AddExpenseViewModel
import com.thang.projectexpensetracker.viewmodel.AddProjectViewModel
import com.thang.projectexpensetracker.viewmodel.ExpenseViewModel
import com.thang.projectexpensetracker.viewmodel.ProjectViewModel
import com.thang.projectexpensetracker.viewmodel.SyncViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themePreferences = remember { ThemePreferences.getInstance(this@MainActivity) }
            val themeMode by themePreferences.themeMode.collectAsState()

            ProjectExpenseTrackerTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                val projectViewModel:    ProjectViewModel    = viewModel()
                val addProjectViewModel: AddProjectViewModel = viewModel()
                val expenseViewModel:    ExpenseViewModel    = viewModel()
                val addExpenseViewModel: AddExpenseViewModel = viewModel()
                val syncViewModel:       SyncViewModel       = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = "admin_dashboard"
                ) {

                    // ── 0. Admin Dashboard (Home) ──────────────────
                    composable("admin_dashboard") {
                        val projects      by projectViewModel.allProjects.collectAsState()
                        val expenseTotals by expenseViewModel.expenseTotalsMap.collectAsState(initial = emptyMap())
                        AdminDashboardScreen(
                            projects       = projects,
                            expenseTotals  = expenseTotals,
                            onAddClick     = {
                                addProjectViewModel.resetProjectForm()
                                navController.navigate("add_project")
                            },
                            onProjectClick = { navController.navigate("project_detail/$it") },
                            onNavigate     = { route ->
                                navController.navigate(route) {
                                    popUpTo("admin_dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onSearchClick  = { navController.navigate("advanced_search") },
                            onMenuClick    = {},
                            onProfileClick = {}
                        )
                    }

                    // ── 1. Add Project Form ────────────────────────
                    composable("add_project") {
                        AddProjectScreen(
                            addProjectViewModel,
                            onNavigateToConfirm = { draft ->
                                addProjectViewModel.setDraftProject(draft)
                                navController.navigate("project_confirmation")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ── 3. Project Confirmation ────────────────────
                    composable("project_confirmation") {
                        val draft      by addProjectViewModel.draftProject.collectAsState()
                        val isEditMode by addProjectViewModel.draftIsEditMode.collectAsState()
                        draft?.let { project ->
                            ConfirmationScreen(
                                project    = project,
                                isEditMode = isEditMode,
                                onConfirm  = {
                                    if (isEditMode) {
                                        addProjectViewModel.updateDraftInDb()
                                        navController.popBackStack()
                                        navController.popBackStack()
                                    } else {
                                        addProjectViewModel.saveDraftToDb()
                                        navController.navigate("admin_dashboard") {
                                            popUpTo("admin_dashboard") { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                onEdit = { navController.popBackStack() }
                            )
                        }
                    }

                    // ── 4. Project Detail ──────────────────────────
                    composable(
                        route = "project_detail/{projectId}",
                        arguments = listOf(navArgument("projectId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
                        val projects  by projectViewModel.allProjects.collectAsState()
                        val project   = projects.find { it.id == projectId }
                        val expenses  by expenseViewModel.getExpensesForProject(projectId).collectAsState(initial = emptyList())

                        project?.let {
                            ProjectDetailScreen(
                                project             = it,
                                expenses            = expenses,
                                onAddExpense        = {
                                    addExpenseViewModel.resetExpenseForm()
                                    navController.navigate("add_expense/$projectId")
                                },
                                onViewAllExpenses   = { navController.navigate("expense_list/$projectId") },
                                onDeleteProject = {
                                    projectViewModel.deleteProject(it)
                                    navController.navigate("admin_dashboard") {
                                        popUpTo("admin_dashboard") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                onDeleteExpense = { expense -> expenseViewModel.deleteExpense(expense) },
                                onEditProject   = { navController.navigate("edit_project/$projectId") },
                                onEditExpense   = { expense -> navController.navigate("edit_expense/${expense.expenseId}") },
                                onNavigateBack  = { navController.popBackStack() }
                            )
                        }
                    }

                    // ── 4b. Expense List Screen ────────────────────
                    composable(
                        route = "expense_list/{projectId}",
                        arguments = listOf(navArgument("projectId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
                        val projects  by projectViewModel.allProjects.collectAsState()
                        val project   = projects.find { it.id == projectId }
                        val expenses  by expenseViewModel.getExpensesForProject(projectId).collectAsState(initial = emptyList())
                        project?.let {
                            ExpenseListScreen(
                                project        = it,
                                expenses       = expenses,
                                onAddExpense   = {
                                    addExpenseViewModel.resetExpenseForm()
                                    navController.navigate("add_expense/$projectId")
                                },
                                onEditExpense  = { expense -> navController.navigate("edit_expense/${expense.expenseId}") },
                                onDeleteExpense = { expense -> expenseViewModel.deleteExpense(expense) },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // ── 5. Add Expense Form ────────────────────────
                    composable(
                        route = "add_expense/{projectId}",
                        arguments = listOf(navArgument("projectId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
                        AddExpenseScreen(
                            viewModel          = addExpenseViewModel,
                            projectId          = projectId,
                            onNavigateToConfirm = { draft ->
                                addExpenseViewModel.setDraftExpense(draft)
                                navController.navigate("expense_confirmation")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ── 6. Expense Confirmation ────────────────────
                    composable("expense_confirmation") {
                        val draft         by addExpenseViewModel.draftExpense.collectAsState()
                        val isEditModeExp by addExpenseViewModel.draftIsEditMode.collectAsState()
                        draft?.let { expense ->
                            ExpenseConfirmationScreen(
                                expense    = expense,
                                isEditMode = isEditModeExp,
                                onConfirm  = {
                                    if (isEditModeExp) {
                                        expenseViewModel.updateExpense(expense)
                                    } else {
                                        expenseViewModel.addExpense(expense)
                                    }
                                    addExpenseViewModel.clearDraft()
                                    if (isEditModeExp) {
                                        navController.popBackStack()
                                        navController.popBackStack()
                                    } else {
                                        navController.popBackStack("add_expense/${expense.projectId}", inclusive = true)
                                    }
                                },
                                onEdit = { navController.popBackStack() }
                            )
                        }
                    }

                    // ── 7+. Edit Project Route ────────────────────
                    composable(
                        route = "edit_project/{projectId}",
                        arguments = listOf(navArgument("projectId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
                        val projects  by projectViewModel.allProjects.collectAsState()
                        val project   = projects.find { it.id == projectId }
                        project?.let {
                            AddProjectScreen(
                                addProjectViewModel,
                                editProject         = it,
                                onNavigateToConfirm = { draft ->
                                    addProjectViewModel.setDraftProject(draft, isEditMode = true)
                                    navController.navigate("project_confirmation")
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // ── 7++. Edit Expense Route ───────────────────
                    composable(
                        route = "edit_expense/{expenseId}",
                        arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: return@composable
                        val expense by expenseViewModel.getExpenseByIdFlow(expenseId).collectAsState(initial = null)
                        expense?.let {
                            AddExpenseScreen(
                                viewModel          = addExpenseViewModel,
                                projectId          = it.projectId,
                                editExpense        = it,
                                onNavigateToConfirm = { draft ->
                                    addExpenseViewModel.setDraftExpense(draft, isEditMode = true)
                                    navController.navigate("expense_confirmation")
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // ── 8. Cloud Sync Screen ───────────────────────
                    composable("sync") {
                        val syncStatus   by syncViewModel.syncStatus.collectAsState()
                        val lastSyncTime by syncViewModel.lastSyncTime.collectAsState()
                        val pendingCount by syncViewModel.pendingSyncCount.collectAsState()
                        val isOffline    by syncViewModel.isOffline.collectAsState()
                        val autoSyncEnabled by syncViewModel.autoSyncEnabled.collectAsState()

                        SyncScreen(
                            syncStatus      = syncStatus,
                            lastSyncTime    = lastSyncTime,
                            pendingCount    = pendingCount,
                            isOffline       = isOffline,
                            autoSyncEnabled = autoSyncEnabled,
                            onSyncNow       = { syncViewModel.triggerSync() },
                            onToggleOffline = { syncViewModel.setOfflineMode(!isOffline) },
                            onToggleAutoSync = { syncViewModel.toggleAutoSync() },
                            onDismissError  = { syncViewModel.dismissSyncError() },
                            onNavigate      = { route ->
                                navController.navigate(route) {
                                    popUpTo("admin_dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ── 9. Advanced Search ─────────────────────────────────
                    composable("advanced_search") {
                        val results by projectViewModel.advancedSearchResults.collectAsState()

                        AdvancedSearchScreen(
                            results = results,
                            onSearch = { query, status, owner, startAfter, endBefore ->
                                projectViewModel.advancedSearch(query, status, owner, startAfter, endBefore)
                            },
                            onClear   = { projectViewModel.clearAdvancedSearch() },
                            onProjectClick = {
                                navController.navigate("project_detail/$it")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ── 10. Settings ───────────────────────────────────────
                    composable("settings") {
                        SettingsScreen(
                            themeMode     = themeMode,
                            onThemeChange = { themePreferences.setThemeMode(it) },
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo("admin_dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
