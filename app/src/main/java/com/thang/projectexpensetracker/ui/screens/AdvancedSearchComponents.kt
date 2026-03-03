package com.thang.projectexpensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import com.thang.projectexpensetracker.ui.components.StatusTag
import com.thang.projectexpensetracker.ui.theme.*

// Screen-scoped pure data — no UI logic
internal val statusOptions = listOf("Active", "On Hold", "Completed", "Cancelled")

// ─────────────────────────────────────────────────────────────────────────────
// AdvancedFilterSheet — bottom sheet with date range, status chips, owner
// Single Responsibility: filter panel rendering + local UI state (dropdown).
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdvancedFilterSheet(
    sheetState: SheetState,
    startAfterStr: String,
    endBeforeStr: String,
    selectedStatus: String?,
    onStatusToggle: (String) -> Unit,
    selectedOwner: String,
    ownerList: List<String>,
    onOwnerSelect: (String) -> Unit,
    onStartPickerOpen: () -> Unit,
    onEndPickerOpen: () -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var ownerDropdownExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AddCardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ProgressTrack)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Advanced Search",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                TextButton(onClick = onReset) {
                    Text("Reset Filters", color = VividBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            // ── Date Range ────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Date Range", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = LabelColor)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DatePickerButton(
                        label    = if (startAfterStr.isBlank()) "Start date" else startAfterStr,
                        modifier = Modifier.weight(1f),
                        onClick  = onStartPickerOpen
                    )
                    DatePickerButton(
                        label    = if (endBeforeStr.isBlank()) "End date" else endBeforeStr,
                        modifier = Modifier.weight(1f),
                        onClick  = onEndPickerOpen
                    )
                }
            }

            // ── Status Chips ──────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Project Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = LabelColor)
                val row1 = statusOptions.take(3)
                val row2 = statusOptions.drop(3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row1.forEach { StatusChipButton(it, selectedStatus == it) { onStatusToggle(it) } }
                    }
                    if (row2.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row2.forEach { StatusChipButton(it, selectedStatus == it) { onStatusToggle(it) } }
                        }
                    }
                }
            }

            // ── Owner Dropdown ────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Project Owner", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = LabelColor)
                ExposedDropdownMenuBox(expanded = ownerDropdownExpanded, onExpandedChange = { ownerDropdownExpanded = it }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                        color = FieldBg
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedOwner, style = MaterialTheme.typography.bodyMedium, color = LabelColor)
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = HintColor)
                        }
                    }
                    ExposedDropdownMenu(expanded = ownerDropdownExpanded, onDismissRequest = { ownerDropdownExpanded = false }) {
                        ownerList.forEach { owner ->
                            DropdownMenuItem(text = { Text(owner) }, onClick = { onOwnerSelect(owner); ownerDropdownExpanded = false })
                        }
                    }
                }
            }

            // ── Apply Button ──────────────────────────────────────────────────
            Button(
                onClick  = onApply,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = VividBlue)
            ) {
                Text("Apply Filters", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date Picker Button  (matches the design image: calendar icon + date text)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun DatePickerButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = FieldBg,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = HintColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = LabelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status Chip  (pill-shaped, blue when selected like the design image)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun StatusChipButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) VividBlue else FieldBg,
        tonalElevation = 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else LabelColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick filter chip (top of main screen, e.g. Date Range / Status)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun QuickChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    iconSize: Dp = 14.dp
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) VividBlue else AddCardBg,
        shadowElevation = if (selected) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) Color.White else HintColor,
                modifier = Modifier.size(iconSize)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else LabelColor
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (selected) Color.White else HintColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Inline search field (no border, sits inside the search bar Surface)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun BasicSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF111827)),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Search
        ),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HintColor
                )
            }
            inner()
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Search Result Card (matches AdminDashboardScreen style)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun AdvancedSearchResultCard(project: ProjectEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.07f),
                spotColor   = Color.Black.copy(alpha = 0.10f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AddCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            // Name + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    project.projectName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                StatusTag(status = project.status)
            }

            // Manager + date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Person, null, tint = HintColor, modifier = Modifier.size(13.dp))
                    Text(project.manager, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = HintColor, modifier = Modifier.size(13.dp))
                    Text(
                        "${project.startDate} → ${project.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}
