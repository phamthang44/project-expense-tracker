package com.thang.projectexpensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import com.thang.projectexpensetracker.ui.components.StatusTag
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ─── Design tokens (shared with AdminDashboardScreen) ────────────────────────
private val SearchPageBg   = Color(0xFFF2F4F7)
private val SearchCardBg   = Color(0xFFFFFFFF)
private val ChipSelected   = Color(0xFF2563EB)          // vivid blue — "Active" chip
private val ChipSelectedText = Color(0xFFFFFFFF)
private val ChipUnselected = Color(0xFFF1F3F6)
private val ChipUnselectedText = Color(0xFF374151)
private val InputBg        = Color(0xFFF1F3F6)
private val ApplyBlue      = Color(0xFF2563EB)
private val LabelGray      = Color(0xFF374151)

private val statusOptions = listOf("Active", "On Hold", "Completed", "Cancelled")
// Owners list used in the dropdown — pulled at runtime from project managers; here we
// keep a static "All Owners" sentinel and build the real list from the projects passed in.
private const val ALL_OWNERS = "All Owners"

// ─────────────────────────────────────────────────────────────────────────────
//  ADVANCED SEARCH SCREEN
//  Layout: TopBar → Search bar → Filter chips row → Results list
//          + ModalBottomSheet filter panel (shown on "Filters" chip tap)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchScreen(
    results: List<ProjectEntity>,
    onSearch: (query: String, status: String?, owner: String?, startAfter: String?, endBefore: String?) -> Unit,
    onClear: () -> Unit,
    onProjectClick: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    // ── Filter state ──────────────────────────────────────────────────────────
    var query          by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedOwner  by remember { mutableStateOf(ALL_OWNERS) }
    var startAfterStr  by remember { mutableStateOf("") }
    var endBeforeStr   by remember { mutableStateOf("") }
    var hasSearched    by remember { mutableStateOf(false) }

    // Bottom sheet
    var showSheet      by remember { mutableStateOf(false) }
    val sheetState     = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Date pickers (inside the sheet)
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }
    val startPickerState = rememberDatePickerState()
    val endPickerState   = rememberDatePickerState()

    // Owner dropdown (inside the sheet)
    var ownerDropdownExpanded by remember { mutableStateOf(false) }

    // Derive owner list from current results + "All Owners" sentinel
    val ownerList = remember(results) {
        listOf(ALL_OWNERS) + results.map { it.manager }.distinct().sorted()
    }

    fun doSearch() {
        hasSearched = true
        onSearch(
            query,
            selectedStatus,
            selectedOwner.takeIf { it != ALL_OWNERS },
            startAfterStr.ifBlank { null },
            endBeforeStr.ifBlank { null }
        )
    }

    fun doReset() {
        query          = ""
        selectedStatus = null
        selectedOwner  = ALL_OWNERS
        startAfterStr  = ""
        endBeforeStr   = ""
        hasSearched    = false
        onClear()
    }

    // ── Date picker dialogs ───────────────────────────────────────────────────
    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startPickerState.selectedDateMillis?.let { startAfterStr = millisToDisplayAdv(it) }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = startPickerState) }
    }
    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endPickerState.selectedDateMillis?.let { endBeforeStr = millisToDisplayAdv(it) }
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = endPickerState) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BOTTOM SHEET — Advanced Filter Panel
    // ─────────────────────────────────────────────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = SearchCardBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                // Drag handle pill
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFD1D5DB))
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
                // ── Sheet header: title + Reset ───────────────────────────────
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
                    TextButton(onClick = { doReset(); showSheet = false }) {
                        Text(
                            "Reset Filters",
                            color = ChipSelected,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                // ── Date Range ────────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Date Range",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = LabelGray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start date picker button
                        DatePickerButton(
                            label = if (startAfterStr.isBlank()) "Oct 01, 2023" else startAfterStr,
                            modifier = Modifier.weight(1f),
                            onClick = { showStartPicker = true }
                        )
                        // End date picker button
                        DatePickerButton(
                            label = if (endBeforeStr.isBlank()) "Oct 31, 2023" else endBeforeStr,
                            modifier = Modifier.weight(1f),
                            onClick = { showEndPicker = true }
                        )
                    }
                }

                // ── Project Status chips ──────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Project Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = LabelGray
                    )
                    // Wrap chips in two rows to match design image
                    val row1 = statusOptions.take(3)
                    val row2 = statusOptions.drop(3)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row1.forEach { status ->
                                StatusChipButton(
                                    label = status,
                                    selected = selectedStatus == status,
                                    onClick = {
                                        selectedStatus = if (selectedStatus == status) null else status
                                    }
                                )
                            }
                        }
                        if (row2.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row2.forEach { status ->
                                    StatusChipButton(
                                        label = status,
                                        selected = selectedStatus == status,
                                        onClick = {
                                            selectedStatus = if (selectedStatus == status) null else status
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Project Owner dropdown ────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Project Owner",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = LabelGray
                    )
                    ExposedDropdownMenuBox(
                        expanded = ownerDropdownExpanded,
                        onExpandedChange = { ownerDropdownExpanded = it }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            color = InputBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedOwner,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF374151)
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280)
                                )
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = ownerDropdownExpanded,
                            onDismissRequest = { ownerDropdownExpanded = false }
                        ) {
                            ownerList.forEach { owner ->
                                DropdownMenuItem(
                                    text = { Text(owner) },
                                    onClick = { selectedOwner = owner; ownerDropdownExpanded = false }
                                )
                            }
                        }
                    }
                }

                // ── Apply Filters button ──────────────────────────────────────
                Button(
                    onClick = { doSearch(); showSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ApplyBlue)
                ) {
                    Text(
                        "Apply Filters",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN SCREEN
    // ─────────────────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = SearchPageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Search Results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onClear(); onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SearchPageBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Search bar ────────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = SearchCardBg,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                    BasicSearchField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Project Name...",
                        modifier = Modifier.weight(1f)
                    )
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { query = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ── Quick filter chips row ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Range chip → opens the bottom sheet
                QuickChip(
                    label = if (startAfterStr.isBlank() && endBeforeStr.isBlank()) "Date Range"
                            else "${startAfterStr.take(5)} – ${endBeforeStr.take(5)}",
                    icon = Icons.Default.CalendarMonth,
                    selected = startAfterStr.isNotBlank() || endBeforeStr.isNotBlank(),
                    onClick = { showSheet = true }
                )
                // Status chip → opens the bottom sheet
                QuickChip(
                    label = selectedStatus ?: "Status",
                    icon = Icons.Default.Circle,
                    iconSize = 8.dp,
                    selected = selectedStatus != null,
                    onClick = { showSheet = true }
                )
                // Search now button
                IconButton(
                    onClick = { doSearch() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ── Results header ────────────────────────────────────────────────
            if (hasSearched) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (results.isEmpty()) "No projects found"
                                   else "${results.size} result${if (results.size != 1) "s" else ""} found",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (results.isEmpty()) MaterialTheme.colorScheme.error
                                    else Color(0xFF374151)
                        )
                        if (results.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "${results.size}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // ── Results list ──────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Empty state
                if (hasSearched && results.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = Color(0xFFD1D5DB)
                            )
                            Text(
                                "No matching projects",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                "Try adjusting your filters",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                // Prompt before first search
                if (!hasSearched) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 56.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.ManageSearch,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = Color(0xFFD1D5DB)
                            )
                            Text(
                                "Search or apply filters",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                "Tap the search icon or open filters",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                // Result cards
                items(results, key = { it.id }) { project ->
                    AdvancedSearchResultCard(
                        project = project,
                        onClick = { onProjectClick(project.id) }
                    )
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date Picker Button  (matches the design image: calendar icon + date text)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DatePickerButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F3F6),
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
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
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
private fun StatusChipButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) ChipSelected else ChipUnselected,
        tonalElevation = 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) ChipSelectedText else ChipUnselectedText
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick filter chip (top of main screen, e.g. Date Range / Status)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuickChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    iconSize: androidx.compose.ui.unit.Dp = 14.dp
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) ChipSelected else SearchCardBg,
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
                tint = if (selected) Color.White else Color(0xFF6B7280),
                modifier = Modifier.size(iconSize)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else Color(0xFF374151)
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (selected) Color.White else Color(0xFF9CA3AF),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Inline search field (no border, sits inside the search bar Surface)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BasicSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
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
                    color = Color(0xFF9CA3AF)
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
private fun AdvancedSearchResultCard(project: ProjectEntity, onClick: () -> Unit) {
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
        colors = CardDefaults.cardColors(containerColor = SearchCardBg),
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
                    Icon(Icons.Default.Person, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(13.dp))
                    Text(project.manager, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(13.dp))
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

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun millisToDisplayAdv(millis: Long): String =
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        .also { it.timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(millis))

private fun formatBudgetSearch(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)
