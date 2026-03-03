package com.thang.projectexpensetracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.data.entity.ProjectEntity

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
                            Text(
                                "$${listFmt(totalSpent)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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
                            Text(
                                "$${listFmt(budget)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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
