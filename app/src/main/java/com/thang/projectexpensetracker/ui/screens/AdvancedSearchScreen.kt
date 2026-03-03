package com.thang.projectexpensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import com.thang.projectexpensetracker.ui.theme.*
import com.thang.projectexpensetracker.util.DateUtils

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
                    startPickerState.selectedDateMillis?.let { startAfterStr = DateUtils.millisToSearchDisplayDate(it) }
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
                    endPickerState.selectedDateMillis?.let { endBeforeStr = DateUtils.millisToSearchDisplayDate(it) }
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
        AdvancedFilterSheet(
            sheetState        = sheetState,
            startAfterStr     = startAfterStr,
            endBeforeStr      = endBeforeStr,
            selectedStatus    = selectedStatus,
            onStatusToggle    = { selectedStatus = if (selectedStatus == it) null else it },
            selectedOwner     = selectedOwner,
            ownerList         = ownerList,
            onOwnerSelect     = { selectedOwner = it },
            onStartPickerOpen = { showStartPicker = true },
            onEndPickerOpen   = { showEndPicker = true },
            onApply           = { doSearch(); showSheet = false },
            onReset           = { doReset(); showSheet = false },
            onDismiss         = { showSheet = false }
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN SCREEN
    // ─────────────────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = AddPageBg,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AddPageBg)
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
                color = AddCardBg,
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
                        tint = HintColor,
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
                                tint = HintColor,
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
                                    else LabelColor
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
                            @Suppress("DEPRECATION")
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
