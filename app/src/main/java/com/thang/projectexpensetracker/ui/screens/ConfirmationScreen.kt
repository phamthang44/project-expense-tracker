package com.thang.projectexpensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import java.text.NumberFormat
import java.util.Locale

// ─── Design tokens ────────────────────────────────────────────────────────────
private val ConfPageBg   = Color(0xFFF2F4F7)
private val ConfCardBg   = Color(0xFFFFFFFF)
private val VividBlue    = Color(0xFF2563EB)
private val LightBlue    = Color(0xFFDBEAFE)
private val LabelGrey    = Color(0xFF6B7280)
private val ValueColor   = Color(0xFF111827)
private val DividerColor = Color(0xFFF3F4F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScreen(
    project: ProjectEntity,
    isEditMode: Boolean = false,
    onConfirm: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        containerColor = ConfPageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Review Changes" else "Final Review",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ConfPageBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Centred header block ──────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Checkmark circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(LightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = VividBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isEditMode) "Confirm Details" else "Confirm Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = ValueColor
                )
                Text(
                    if (isEditMode) "Please verify your changes before saving."
                    else "Please verify all project details are correct\nbefore saving.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LabelGrey,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            // ── Project Details card ──────────────────────────────────────────
            ReviewCard {
                // Card header label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, null, tint = VividBlue, modifier = Modifier.size(16.dp))
                    Text(
                        "PROJECT DETAILS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = VividBlue,
                        letterSpacing = 1.sp
                    )
                }

                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))

                // Project Name + Status side-by-side
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ReviewField(label = "Project Name", value = project.projectName, modifier = Modifier.weight(1f))
                    ConfStatusBadge(status = project.status)
                }

                HorizontalDivider(color = DividerColor)

                // Project ID + Priority
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ReviewField(label = "Project ID", value = project.projectCode, modifier = Modifier.weight(1f))
                    ReviewField(label = "Priority", value = project.priority, modifier = Modifier.weight(1f))
                }

                HorizontalDivider(color = DividerColor)

                // Manager + Budget
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ReviewField(label = "Manager", value = project.manager, modifier = Modifier.weight(1f))
                    ReviewField(label = "Budget", value = "\$${formatBudget(project.budget)}", modifier = Modifier.weight(1f))
                }

                // Description (if filled)
                if (project.description.isNotBlank()) {
                    HorizontalDivider(color = DividerColor)
                    ReviewField(label = "Description", value = project.description)
                }

                // Client Info (if filled)
                if (!project.clientInfo.isNullOrBlank()) {
                    HorizontalDivider(color = DividerColor)
                    ReviewField(label = "Client / Department", value = project.clientInfo)
                }

                // Special Requirements (if filled)
                if (!project.specialRequirements.isNullOrBlank()) {
                    HorizontalDivider(color = DividerColor)
                    ReviewField(label = "Special Requirements", value = project.specialRequirements)
                }
            }

            // ── Schedule card ─────────────────────────────────────────────────
            ReviewCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = VividBlue, modifier = Modifier.size(16.dp))
                    Text(
                        "SCHEDULE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = VividBlue,
                        letterSpacing = 1.sp
                    )
                }

                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ReviewField(label = "Start Date", value = project.startDate, modifier = Modifier.weight(1f))
                    ReviewField(label = "End Date",   value = project.endDate,   modifier = Modifier.weight(1f))
                }
            }

            // ── Total Budget banner ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(VividBlue)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "TOTAL BUDGET",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.75f),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "\$${formatBudget(project.budget)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "CURRENCY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.75f),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "USD (\$)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // ── Disclaimer block ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightBlue)
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = VividBlue,
                        modifier = Modifier.size(18.dp).padding(top = 1.dp)
                    )
                    Text(
                        "By clicking \"Confirm & Save\", you confirm that all project details are accurate and ready to be recorded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 18.sp
                    )
                }
            }

            // ── Action buttons ────────────────────────────────────────────────
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VividBlue)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEditMode) "Confirm & Save" else "Confirm & Save",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            TextButton(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Edit Entry",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// White card wrapper
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ReviewCard(content: @Composable ColumnScope.() -> Unit) {
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
private fun ReviewField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = LabelGrey)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ValueColor)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status badge — pill matching design style
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ConfStatusBadge(status: String) {
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
private fun formatBudget(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)
