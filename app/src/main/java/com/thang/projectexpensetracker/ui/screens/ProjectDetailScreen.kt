package com.thang.projectexpensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import java.text.NumberFormat
import java.util.Locale

// ─── Design tokens ────────────────────────────────────────────────────────────
private val DetailBg     = Color(0xFFF2F4F7)
private val DetailCard   = Color(0xFFFFFFFF)
private val VividBlue    = Color(0xFF2563EB)
private val LabelGrey    = Color(0xFF6B7280)
private val ValueDark    = Color(0xFF111827)
private val DivLine      = Color(0xFFF3F4F6)

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
                        Icon(Icons.Default.Edit, "Edit", tint = VividBlue)
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
                containerColor = VividBlue,
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
                            color      = VividBlue,
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

// ─────────────────────────────────────────────────────────────────────────────
// 1. Project Info Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProjectInfoCard(project: ProjectEntity) {
    DetailCard {
        // Status badge + Project ID
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailStatusBadge(project.status)
            Text(
                "ID: ${project.projectCode}",
                style = MaterialTheme.typography.labelSmall,
                color = LabelGrey
            )
        }

        Spacer(Modifier.height(6.dp))

        // Project Name (large)
        Text(
            project.projectName,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color      = ValueDark,
            lineHeight = 28.sp
        )

        // Description
        if (project.description.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                project.description,
                style    = MaterialTheme.typography.bodySmall,
                color    = LabelGrey,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = DivLine)
        Spacer(Modifier.height(8.dp))

        // Info grid: Manager | Client (if any)  /  Start Date | End Date
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            DetailInfoCell("MANAGER",    project.manager,    modifier = Modifier.weight(1f))
            if (!project.clientInfo.isNullOrBlank()) {
                DetailInfoCell("CLIENT", project.clientInfo, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            DetailInfoCell("START DATE", project.startDate, modifier = Modifier.weight(1f))
            DetailInfoCell("END DATE",   project.endDate,   modifier = Modifier.weight(1f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Budget Performance Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BudgetPerformanceCard(
    budget: Double,
    totalExpenses: Double,
    budgetUsed: Float,
    budgetPct: Int
) {
    val barColor = when {
        budgetUsed >= 0.9f -> Color(0xFFDC2626)
        budgetUsed >= 0.7f -> Color(0xFFF59E0B)
        else               -> VividBlue
    }

    DetailCard {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier            = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFDBEAFE)),
                    contentAlignment    = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalance, null, tint = VividBlue, modifier = Modifier.size(18.dp))
                }
                Text(
                    "Budget Performance",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = ValueDark
                )
            }
            Text(
                "$budgetPct% Used",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color      = barColor
            )
        }

        Spacer(Modifier.height(10.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE5E7EB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(budgetUsed)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Actual Spending vs Total Budget
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Actual Spending", style = MaterialTheme.typography.labelSmall, color = LabelGrey)
                Spacer(Modifier.height(2.dp))
                Text(
                    "\$${fmtAmt(totalExpenses)}",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = ValueDark
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Total Budget", style = MaterialTheme.typography.labelSmall, color = LabelGrey)
                Spacer(Modifier.height(2.dp))
                Text(
                    "\$${fmtAmt(budget)}",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = LabelGrey
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Expense Row (compact list inside card)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExpenseRow(
    expense: ExpenseEntity,
    onClick: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val iconBg  = expenseIconBg(expense.type)
    val icon    = expenseIcon(expense.type)

    Row(
        modifier              = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Category icon circle
        Box(
            modifier         = Modifier.size(40.dp).clip(CircleShape).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }

        // Name + meta
        Column(modifier = Modifier.weight(1f)) {
            Text(
                expense.description?.ifBlank { expense.type } ?: expense.type,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = ValueDark,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                "${expense.date}  •  ${expense.type}",
                style = MaterialTheme.typography.labelSmall,
                color = LabelGrey
            )
        }

        // Amount + status + more
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "\$${fmtAmt(expense.amount)}",
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color      = ValueDark
            )
            ExpenseStatusPill(expense.paymentStatus)
        }

        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(16.dp), tint = LabelGrey)
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text        = { Text("Edit") },
                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                    onClick     = { onEdit(); showMenu = false }
                )
                DropdownMenuItem(
                    text        = { Text("Delete", color = Color(0xFFDC2626)) },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFDC2626)) },
                    onClick     = { onDelete(); showMenu = false }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. Special requirements card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SpecialRequirementsCard(text: String) {
    DetailCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.PriorityHigh, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
            Text(
                "Special Requirements",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = ValueDark
            )
        }
        Spacer(Modifier.height(8.dp))
        // Split by sentence / line and render bullet points
        val lines = text.split(".")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (lines.size > 1) {
            lines.forEach { line ->
                Row(
                    modifier              = Modifier.padding(vertical = 3.dp),
                    verticalAlignment     = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Text("$line.", style = MaterialTheme.typography.bodySmall, color = ValueDark, lineHeight = 18.sp)
                }
            }
        } else {
            Text(text, style = MaterialTheme.typography.bodySmall, color = ValueDark, lineHeight = 18.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable white card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = DetailCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Label + value pair cell
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DetailInfoCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = LabelGrey, letterSpacing = 0.5.sp)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ValueDark)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status badge
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DetailStatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "Active"    -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
        "On Hold"   -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        "Completed" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        else        -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
    }
    Box(
        modifier         = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            status.uppercase(),
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color      = fg,
            letterSpacing = 0.5.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Expense payment status pill
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExpenseStatusPill(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "paid"        -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "reimbursed"  -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
        "pending"     -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        else          -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(bg).padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            status.uppercase(),
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color      = fg,
            fontSize   = 9.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Expense category icon + color helpers
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun expenseIconBg(type: String): Color = when (type.lowercase()) {
    "travel", "transport"   -> Color(0xFF7C3AED)
    "food", "meals"         -> Color(0xFFDB2777)
    "equipment", "hardware" -> Color(0xFF2563EB)
    "materials"             -> Color(0xFFEA580C)
    "accommodation"         -> Color(0xFF0891B2)
    "software"              -> Color(0xFF16A34A)
    else                    -> Color(0xFF6B7280)
}

private fun expenseIcon(type: String): ImageVector = when (type.lowercase()) {
    "travel", "transport"   -> Icons.Default.Flight
    "food", "meals"         -> Icons.Default.Restaurant
    "equipment", "hardware" -> Icons.Default.Computer
    "accommodation"         -> Icons.Default.Hotel
    "software"              -> Icons.Default.Code
    else                    -> Icons.Default.Build
}

// ─────────────────────────────────────────────────────────────────────────────
// Number formatter
// ─────────────────────────────────────────────────────────────────────────────
private fun fmtAmt(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)
