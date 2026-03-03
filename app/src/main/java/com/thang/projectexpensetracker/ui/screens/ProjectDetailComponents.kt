package com.thang.projectexpensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

// ─── Design tokens (shared with ProjectDetailScreen) ─────────────────────────
internal val DetailBg     = Color(0xFFF2F4F7)
internal val DetailCard   = Color(0xFFFFFFFF)
internal val PdVividBlue  = Color(0xFF2563EB)
internal val LabelGrey    = Color(0xFF6B7280)
internal val ValueDark    = Color(0xFF111827)
internal val DivLine      = Color(0xFFF3F4F6)

// ─────────────────────────────────────────────────────────────────────────────
// 1. Project Info Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun ProjectInfoCard(project: ProjectEntity) {
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
internal fun BudgetPerformanceCard(
    budget: Double,
    totalExpenses: Double,
    budgetUsed: Float,
    budgetPct: Int
) {
    val barColor = when {
        budgetUsed >= 0.9f -> Color(0xFFDC2626)
        budgetUsed >= 0.7f -> Color(0xFFF59E0B)
        else               -> PdVividBlue
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
                    Icon(Icons.Default.AccountBalance, null, tint = PdVividBlue, modifier = Modifier.size(18.dp))
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
internal fun ExpenseRow(
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
internal fun SpecialRequirementsCard(text: String) {
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
internal fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
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
internal fun DetailInfoCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = LabelGrey, letterSpacing = 0.5.sp)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ValueDark)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status badge
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun DetailStatusBadge(status: String) {
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
internal fun ExpenseStatusPill(status: String) {
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
internal fun expenseIconBg(type: String): Color = when (type.lowercase()) {
    "travel", "transport"   -> Color(0xFF7C3AED)
    "food", "meals"         -> Color(0xFFDB2777)
    "equipment", "hardware" -> Color(0xFF2563EB)
    "materials"             -> Color(0xFFEA580C)
    "accommodation"         -> Color(0xFF0891B2)
    "software"              -> Color(0xFF16A34A)
    else                    -> Color(0xFF6B7280)
}

internal fun expenseIcon(type: String): ImageVector = when (type.lowercase()) {
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
internal fun fmtAmt(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)
