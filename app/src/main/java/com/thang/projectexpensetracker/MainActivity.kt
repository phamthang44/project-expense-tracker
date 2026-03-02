package com.thang.projectexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thang.projectexpensetracker.ui.screens.*
import com.thang.projectexpensetracker.ui.theme.ProjectExpenseTrackerTheme
import com.thang.projectexpensetracker.viewmodel.ProjectViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectExpenseTrackerTheme {
                val navController = rememberNavController()
                val viewModel: ProjectViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = "admin_dashboard"
                ) {

                    // ── 0. Admin Dashboard (Home) ──────────────────
                    composable("admin_dashboard") {
                        val projects      by viewModel.allProjects.collectAsState()
                        val expenseTotals by viewModel.expenseTotalsMap.collectAsState(initial = emptyMap())
                        AdminDashboardScreen(
                            projects       = projects,
                            expenseTotals  = expenseTotals,
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

                    // ── 1. Project Dashboard (List) ────────────────
                    composable("project_list") {
                        val projects   by viewModel.allProjects.collectAsState()
                        ProjectListScreen(
                            projects        = projects,
                            onSearch        = { viewModel.searchProjects(it) },
                            onAddClick      = { navController.navigate("add_project") },
                            onProjectClick  = { navController.navigate("project_detail/$it") },
                            onDeleteProject = { viewModel.deleteProject(it) },
                            onSyncClick     = { navController.navigate("sync") },
                            onAdvancedSearchClick = { navController.navigate("advanced_search") }
                        )
                    }

                    // ── 2. Add Project Form ────────────────────────
                    composable("add_project") {
                        AddProjectScreen(
                            onNavigateToConfirm = { draft ->
                                viewModel.setDraftProject(draft)
                                navController.navigate("project_confirmation")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ── 3. Project Confirmation ────────────────────
                    composable("project_confirmation") {
                        val draft      by viewModel.draftProject.collectAsState()
                        val isEditMode by viewModel.draftIsEditMode.collectAsState()
                        draft?.let { project ->
                            ConfirmationScreen(
                                project    = project,
                                isEditMode = isEditMode,
                                onConfirm  = {
                                    if (isEditMode) {
                                        viewModel.updateDraftInDb()
                                        // Pop back to project detail (2 screens: confirmation + edit form)
                                        navController.popBackStack()
                                        navController.popBackStack()
                                    } else {
                                        viewModel.saveDraftToDb()
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
                        val projects  by viewModel.allProjects.collectAsState()
                        val project   = projects.find { it.id == projectId }
                        val expenses  by viewModel.getExpensesForProject(projectId).collectAsState(initial = emptyList())

                        project?.let {
                            ProjectDetailScreen(
                                project             = it,
                                expenses            = expenses,
                                onAddExpense        = { navController.navigate("add_expense/$projectId") },
                                onViewAllExpenses   = { navController.navigate("expense_list/$projectId") },
                                onDeleteProject = {
                                    viewModel.deleteProject(it)
                                    navController.navigate("admin_dashboard") {
                                        popUpTo("admin_dashboard") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                onDeleteExpense = { expense -> viewModel.deleteExpense(expense) },
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
                        val projects  by viewModel.allProjects.collectAsState()
                        val project   = projects.find { it.id == projectId }
                        val expenses  by viewModel.getExpensesForProject(projectId).collectAsState(initial = emptyList())
                        project?.let {
                            ExpenseListScreen(
                                project        = it,
                                expenses       = expenses,
                                onAddExpense   = { navController.navigate("add_expense/$projectId") },
                                onEditExpense  = { expense -> navController.navigate("edit_expense/${expense.expenseId}") },
                                onDeleteExpense = { expense -> viewModel.deleteExpense(expense) },
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
                            projectId          = projectId,
                            onNavigateToConfirm = { draft ->
                                viewModel.setDraftExpense(draft)
                                navController.navigate("expense_confirmation")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ── 6. Expense Confirmation ────────────────────
                    composable("expense_confirmation") {
                        val draft         by viewModel.draftExpense.collectAsState()
                        val isEditModeExp by viewModel.draftIsEditModeExpense.collectAsState()
                        draft?.let { expense ->
                            ExpenseConfirmationScreen(
                                expense    = expense,
                                isEditMode = isEditModeExp,
                                onConfirm  = {
                                    viewModel.saveDraftExpense()
                                    if (isEditModeExp) {
                                        // Pop confirmation + edit form back to project detail
                                        navController.popBackStack()
                                        navController.popBackStack()
                                    } else {
                                        // Pop confirmation + add form back to project detail
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
                        val projects  by viewModel.allProjects.collectAsState()
                        val project   = projects.find { it.id == projectId }
                        project?.let {
                            AddProjectScreen(
                                editProject         = it,
                                onNavigateToConfirm = { draft ->
                                    viewModel.setDraftProject(draft, isEditMode = true)
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
                        val expense by viewModel.getExpenseByIdFlow(expenseId).collectAsState(initial = null)
                        expense?.let {
                            AddExpenseScreen(
                                projectId          = it.projectId,
                                editExpense        = it,
                                onNavigateToConfirm = { draft ->
                                    viewModel.setDraftExpense(draft, isEditMode = true)
                                    navController.navigate("expense_confirmation")
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // ── 8. Cloud Sync Screen ───────────────────────
                    composable("sync") {
                        val syncStatus   by viewModel.syncStatus.collectAsState()
                        val lastSyncTime by viewModel.lastSyncTime.collectAsState()
                        val pendingCount by viewModel.pendingSyncCount.collectAsState()
                        val isOffline    by viewModel.isOffline.collectAsState()

                        SyncScreen(
                            syncStatus     = syncStatus,
                            lastSyncTime   = lastSyncTime,
                            pendingCount   = pendingCount,
                            isOffline      = isOffline,
                            onSyncNow      = { viewModel.triggerSync() },
                            onToggleOffline = { viewModel.setOfflineMode(!isOffline) },
                            onDismissError = { viewModel.dismissSyncError() },
                            onNavigate     = { route ->
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
                        val results by viewModel.advancedSearchResults.collectAsState()

                        AdvancedSearchScreen(
                            results = results,
                            onSearch = { query, status, owner, startAfter, endBefore ->
                                viewModel.advancedSearch(query, status, owner, startAfter, endBefore)
                            },
                            onClear   = { viewModel.clearAdvancedSearch() },
                            onProjectClick = {
                                navController.navigate("project_detail/$it")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ── 10. Settings ───────────────────────────────────────
                    composable("settings") {
                        SettingsScreen(
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
