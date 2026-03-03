package com.thang.projectexpensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

// ─── Design tokens (shared with ConfirmationScreen) ──────────────────────────
internal val ConfPageBg   = Color(0xFFF2F4F7)
internal val ConfCardBg   = Color(0xFFFFFFFF)
internal val ConfVividBlue = Color(0xFF2563EB)
internal val ConfLightBlue = Color(0xFFDBEAFE)
internal val ConfLabelGrey = Color(0xFF6B7280)
internal val ConfValueColor = Color(0xFF111827)
internal val ConfDividerColor = Color(0xFFF3F4F6)

// ─────────────────────────────────────────────────────────────────────────────
// White card wrapper
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun ReviewCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ConfCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Single label + value pair
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun ReviewField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = ConfLabelGrey)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ConfValueColor)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status badge — pill matching design style
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun ConfStatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "Active"    -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "On Hold"   -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        "Completed" -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
        else        -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            letterSpacing = 0.5.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Budget formatter
// ─────────────────────────────────────────────────────────────────────────────
internal fun confFormatBudget(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)
