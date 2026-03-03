package com.thang.projectexpensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import java.text.NumberFormat
import java.util.Locale

// ─── Design tokens (shared with ExpenseListScreen) ───────────────────────────
internal val ElBg        = Color(0xFFF2F4F7)
internal val ElCard      = Color(0xFFFFFFFF)
internal val ElBlue      = Color(0xFF2563EB)
internal val ElDark      = Color(0xFF111827)
internal val ElGrey      = Color(0xFF6B7280)
internal val ElTrack     = Color(0xFFE5E7EB)
internal val ElHero      = Color(0xFFF0F4FF)

internal fun listFmt(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }.format(amount)

// ─── Category icon/color mapping ──────────────────────────────────────────────
internal data class CategoryStyle(val icon: ImageVector, val containerColor: Color, val iconTint: Color)

internal fun categoryStyle(type: String): CategoryStyle = when {
    type.contains("travel", ignoreCase = true) || type.contains("transport", ignoreCase = true) ->
        CategoryStyle(Icons.Default.Flight, Color(0xFFEDE9FE), Color(0xFF7C3AED))
    type.contains("food", ignoreCase = true) || type.contains("meal", ignoreCase = true) || type.contains("entertainment", ignoreCase = true) ->
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
internal fun elStatusColors(status: String): Pair<Color, Color> = when {
    status.equals("Paid",       ignoreCase = true) -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
    status.equals("Approved",   ignoreCase = true) -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
    status.equals("Reimbursed", ignoreCase = true) -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
    else                                            -> Color(0xFFFEF3C7) to Color(0xFFD97706)  // Pending
}

// ─────────────────────────────────────────────────────────────────────────────
// Single expense row card  (matches design image)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun ExpenseListRow(
    expense: ExpenseEntity,
    onEdit:   () -> Unit,
    onDelete: () -> Unit
) {
    val style                     = categoryStyle(expense.type)
    val (statusBg, statusColor)   = elStatusColors(expense.paymentStatus)

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
