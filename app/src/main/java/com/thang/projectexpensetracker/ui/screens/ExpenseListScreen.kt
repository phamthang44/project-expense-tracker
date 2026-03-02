package com.thang.projectexpensetracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import java.text.NumberFormat
import java.util.Locale

// ─── Design tokens ────────────────────────────────────────────────────────────
private val ElBg        = Color(0xFFF2F4F7)
private val ElCard      = Color(0xFFFFFFFF)
private val ElBlue      = Color(0xFF2563EB)
private val ElDark      = Color(0xFF111827)
private val ElGrey      = Color(0xFF6B7280)
private val ElTrack     = Color(0xFFE5E7EB)
private val ElHero      = Color(0xFFF0F4FF)

private fun listFmt(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 2; minimumFractionDigits = 2 }.format(amount)

// ─── Category icon/color mapping ──────────────────────────────────────────────
private data class CategoryStyle(val icon: ImageVector, val containerColor: Color, val iconTint: Color)

private fun categoryStyle(type: String): CategoryStyle = when {
    type.contains("travel", ignoreCase = true) || type.contains("transport", ignoreCase = true) ->
        CategoryStyle(Icons.Default.Flight, Color(0xFFEDE9FE), Color(0xFF7C3AED))
    type.contains("food", ignoreCase = true)  || type.contains("meal",  ignoreCase = true) || type.contains("entertainment", ignoreCase = true) ->
        CategoryStyle(Icons.Default.Restaurant, Color(0xFFFCE7F3), Color(0xFFDB2777))
    type.contains("equip", ignoreCase = true) || type.contains("hardware", ignoreCase = true) ->
        CategoryStyle(Icons.Default.Computer, Color(0xFFDBEAFE), Color(0xFF2563EB))
    type.contains("material", ignoreCase = true) ->
        CategoryStyle(Icons.Default.Build, Color(0xFFFFEDD5), Color(0xFFEA580C))
    type.contains("accom", ignoreCase = true) || type.contains("hotel", ignoreCase = true) ->
        CategoryStyle(Icons.Default.Hotel, Color(0xFFCCFBF1), Color(0xFF0D9488))
    type.contains("software", ignoreCase = true) ->
        CategoryStyle(Icons.Default.Code, Color(0xFFDCFCE7), Color(0xFF16A34A))
    type.contains("labour", ignoreCase = true) || type.contains("office", ignoreCase = true) ->
        CategoryStyle(Icons.Default.Work, Color(0xFFFEF9C3), Color(0xFFCA8A04))
    else ->
        CategoryStyle(Icons.Default.Receipt, Color(0xFFF3F4F6), Color(0xFF6B7280))
}

// ─── Status pill colors ───────────────────────────────────────────────────────
private fun statusColors(status: String): Pair<Color, Color> = when {
    status.equals("Paid",        ignoreCase = true) -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
    status.equals("Approved",    ignoreCase = true) -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
    status.equals("Reimbursed",  ignoreCase = true) -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
    else                                             -> Color(0xFFFEF3C7) to Color(0xFFD97706)  // Pending
}

// ─────────────────────────────────────────────────────────────────────────────
// ExpenseListScreen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    project: ProjectEntity,
    expenses: List<ExpenseEntity>,
    onAddExpense: () -> Unit = {},
    onEditExpense: (ExpenseEntity) -> Unit = {},
    onDeleteExpense: (ExpenseEntity) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val totalSpent  = expenses.sumOf { it.amount }
    val budget      = project.budget
    val usedFraction = if (budget > 0) (totalSpent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val animFraction by animateFloatAsState(targetValue = usedFraction, animationSpec = tween(900), label = "budget")

    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    // Delete confirm dialog
    expenseToDelete?.let { exp ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title            = { Text("Delete Expense") },
            text             = { Text("Remove expense \"$${listFmt(exp.amount)} - ${exp.type}\"? This cannot be undone.") },
            confirmButton    = {
                TextButton(onClick = { onDeleteExpense(exp); expenseToDelete = null }) {
                    Text("Delete", color = Color(0xFFDC2626))
                }
            },
            dismissButton = { TextButton(onClick = { expenseToDelete = null }) { Text("Cancel") } },
            shape            = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = ElBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Project Expenses",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = ElDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, "Search", tint = ElDark)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, "Filter", tint = ElDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ElBg)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = onAddExpense,
                containerColor = ElBlue,
                contentColor   = Color.White,
                shape          = RoundedCornerShape(16.dp),
                icon           = { Icon(Icons.Default.Add, "Add Expense") },
                text           = { Text("Add Expense", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier             = Modifier.fillMaxSize().padding(padding),
            contentPadding       = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement  = Arrangement.spacedBy(12.dp)
        ) {
            // ── Summary cards ─────────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Spent card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = ElCard),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Receipt, null, tint = ElGrey, modifier = Modifier.size(14.dp))
                                Text("Total Spent", style = MaterialTheme.typography.bodySmall, color = ElGrey)
                            }
                            Text("$${listFmt(totalSpent)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = ElDark)
                            if (expenses.isNotEmpty()) {
                                Text(
                                    "↑ ${expenses.size} expense${if (expenses.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Total Budget card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = ElCard),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.AccountBalance, null, tint = ElGrey, modifier = Modifier.size(14.dp))
                                Text("Total Budget", style = MaterialTheme.typography.bodySmall, color = ElGrey)
                            }
                            Text("$${listFmt(budget)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = ElDark)
                            // Mini progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(ElTrack)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animFraction)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (usedFraction >= 0.90f) Color(0xFFDC2626)
                                            else if (usedFraction >= 0.70f) Color(0xFFD97706)
                                            else ElBlue
                                        )
                                )
                            }
                            Text(
                                "${(usedFraction * 100).toInt()}% utilized",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElGrey
                            )
                        }
                    }
                }
            }

            // ── Section title ─────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Transaction History",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = ElDark
                )
            }

            // ── Expense rows ──────────────────────────────────────────────────
            if (expenses.isEmpty()) {
                item {
                    Box(
                        modifier          = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment  = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.ReceiptLong, null, tint = ElGrey, modifier = Modifier.size(48.dp))
                            Text("No expenses yet", style = MaterialTheme.typography.bodyLarge, color = ElGrey)
                            Text("Tap 'Add Expense' to record your first expense", style = MaterialTheme.typography.bodySmall, color = ElGrey)
                        }
                    }
                }
            } else {
                items(expenses, key = { it.expenseId }) { expense ->
                    ExpenseListRow(
                        expense    = expense,
                        onEdit     = { onEditExpense(expense) },
                        onDelete   = { expenseToDelete = expense }
                    )
                }
            }

            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Single expense row card  (matches design image)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExpenseListRow(
    expense: ExpenseEntity,
    onEdit:   () -> Unit,
    onDelete: () -> Unit
) {
    val style                     = categoryStyle(expense.type)
    val (statusBg, statusColor)   = statusColors(expense.paymentStatus)

    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = ElCard),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category icon circle
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(style.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(style.icon, null, tint = style.iconTint, modifier = Modifier.size(24.dp))
            }

            // Details
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "$${listFmt(expense.amount)}",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = ElDark,
                    fontSize   = 16.sp
                )
                Text(
                    "${expense.type} • ${expense.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElGrey
                )
                Text(
                    "Claimant: ${expense.claimant}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElGrey
                )
            }

            // Actions column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", tint = ElGrey, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    }
                }
                // Status pill
                Box(
                    modifier         = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        expense.paymentStatus,
                        style      = MaterialTheme.typography.labelSmall,
                        color      = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
