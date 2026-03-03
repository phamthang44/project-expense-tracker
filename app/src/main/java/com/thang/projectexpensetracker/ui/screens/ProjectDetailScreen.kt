package com.thang.projectexpensetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.data.entity.ProjectEntity

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    project: ProjectEntity,
    expenses: List<ExpenseEntity>,
    onAddExpense: () -> Unit,
    onViewAllExpenses: () -> Unit = {},
    onDeleteProject: () -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit = {},
    onEditProject: () -> Unit = {},
    onEditExpense: (ExpenseEntity) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val totalExpenses = expenses.sumOf { it.amount }
    val budgetUsed    = if (project.budget > 0)
        (totalExpenses / project.budget).coerceIn(0.0, 1.0).toFloat() else 0f
    val budgetPct     = (budgetUsed * 100).toInt()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu         by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon  = { Icon(Icons.Default.Warning, null, tint = Color(0xFFDC2626)) },
            title = { Text("Delete Project?") },
            text  = { Text("This will permanently delete \"${project.projectName}\" and all its expenses.") },
            confirmButton = {
                Button(
                    onClick = { onDeleteProject(); showDeleteDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = DetailBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Project Details",
                        style     = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditProject) {
                        Icon(Icons.Default.Edit, "Edit", tint = PdVividBlue)
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text         = { Text("Delete Project", color = Color(0xFFDC2626)) },
                                leadingIcon  = { Icon(Icons.Default.Delete, null, tint = Color(0xFFDC2626)) },
                                onClick      = { showMenu = false; showDeleteDialog = true }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DetailBg)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = onAddExpense,
                containerColor = PdVividBlue,
                contentColor   = Color.White,
                shape          = RoundedCornerShape(16.dp),
                icon           = { Icon(Icons.Default.Add, "Add Expense", modifier = Modifier.size(18.dp)) },
                text           = {
                    Text(
                        "Add Expense",
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(padding),
            contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── 1. Project Info card ──────────────────────────────────────────
            item {
                ProjectInfoCard(project = project)
            }

            // ── 2. Budget Performance card ────────────────────────────────────
            item {
                BudgetPerformanceCard(
                    budget        = project.budget,
                    totalExpenses = totalExpenses,
                    budgetUsed    = budgetUsed,
                    budgetPct     = budgetPct
                )
            }

            // ── 3. Recent Expenses header ─────────────────────────────────────
            item {
                Row(
                    modifier                = Modifier.fillMaxWidth(),
                    horizontalArrangement   = Arrangement.SpaceBetween,
                    verticalAlignment       = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Expenses",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = ValueDark
                    )
                    if (expenses.isNotEmpty()) {
                        Text(
                            "View All",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = PdVividBlue,
                            modifier   = Modifier.clickable { onViewAllExpenses() }
                        )
                    }
                }
            }

            // ── 4. Expenses list OR empty state ───────────────────────────────
            if (expenses.isEmpty()) {
                item {
                    DetailCard {
                        Column(
                            modifier               = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment    = Alignment.CenterHorizontally,
                            verticalArrangement    = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💰", fontSize = 36.sp)
                            Text(
                                "No expenses recorded",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = ValueDark
                            )
                            Text(
                                "Tap + to log the first expense",
                                style = MaterialTheme.typography.bodySmall,
                                color = LabelGrey
                            )
                        }
                    }
                }
            } else {
                item {
                    DetailCard {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            expenses.take(5).forEachIndexed { index, expense ->
                                ExpenseRow(
                                    expense  = expense,
                                    onClick  = { onEditExpense(expense) },
                                    onEdit   = { onEditExpense(expense) },
                                    onDelete = { onDeleteExpense(expense) }
                                )
                                if (index < minOf(expenses.size, 5) - 1) {
                                    HorizontalDivider(color = DivLine, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ── 5. Special Requirements (if any) ─────────────────────────────
            if (!project.specialRequirements.isNullOrBlank()) {
                item {
                    SpecialRequirementsCard(text = project.specialRequirements)
                }
            }

            // ── 6. Bottom spacer for FAB ──────────────────────────────────────
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}
