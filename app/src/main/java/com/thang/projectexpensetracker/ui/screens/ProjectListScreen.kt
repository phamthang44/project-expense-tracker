package com.thang.projectexpensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import com.thang.projectexpensetracker.ui.components.PriorityTag
import com.thang.projectexpensetracker.ui.components.StatusTag
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    projects: List<ProjectEntity>,
    onSearch: (String) -> Unit,
    onAddClick: () -> Unit,
    onProjectClick: (Long) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit = {},
    onSyncClick: () -> Unit = {},
    onAdvancedSearchClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    // Confirm-delete dialog
    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Project?") },
            text = {
                Text("\"${project.projectName}\" and all its expenses will be permanently removed. This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = { onDeleteProject(project); projectToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { projectToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Expense Tracker", fontWeight = FontWeight.Bold)
                        Text(
                            "${projects.size} project${if (projects.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Advanced Search
                    IconButton(onClick = onAdvancedSearchClick) {
                        Icon(Icons.Default.ManageSearch, contentDescription = "Advanced Search")
                    }
                    // Cloud Sync
                    IconButton(onClick = onSyncClick) {
                        Icon(Icons.Default.Refresh, contentDescription = "Cloud Sync")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Project") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it; onSearch(it) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    placeholder = { Text("Quick search by name or description…") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        AnimatedVisibility(searchQuery.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                            IconButton(onClick = { searchQuery = ""; onSearch("") }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Stats row
            item { DashboardStatsRow(projects = projects) }
            item { Spacer(Modifier.height(16.dp)) }

            // Section header
            item {
                Text(
                    text = if (projects.isEmpty()) "No Projects" else "All Projects (${projects.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (projects.isEmpty()) {
                item { EmptyStateCard() }
            }

            items(projects, key = { it.id }) { project ->
                SwipeToDeleteProjectCard(
                    project = project,
                    onClick = { onProjectClick(project.id) },
                    onDelete = { projectToDelete = project }
                )
                Spacer(Modifier.height(10.dp))
            }

            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dashboard Stats Row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DashboardStatsRow(projects: List<ProjectEntity>) {
    val totalBudget    = projects.sumOf { it.budget }
    val activeCount    = projects.count { it.status.equals("Active", true) }
    val completedCount = projects.count { it.status.equals("Completed", true) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatChip("Active",    "$activeCount",              MaterialTheme.colorScheme.primaryContainer,  MaterialTheme.colorScheme.onPrimaryContainer,  Modifier.weight(1f))
        StatChip("Done",      "$completedCount",           MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Modifier.weight(1f))
        StatChip("Budget",    "$${formatBudget(totalBudget)}", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.75f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Swipe-to-Delete wrapper
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteProjectCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete() }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val scale by animateFloatAsState(
                targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) 1f else 0.75f,
                label = "deleteScale"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(16.dp))
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.scale(scale))
                    Text("Delete", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    ) {
        ProjectCard(project = project, onClick = onClick)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Project Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProjectCard(project: ProjectEntity, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(project.projectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(project.projectCode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusTag(status = project.status)
            }

            if (project.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(project.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(project.manager, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (project.priority != "Normal") {
                        PriorityTag(priority = project.priority)
                        Spacer(Modifier.width(8.dp))
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            "$${formatBudget(project.budget)}",
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
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EmptyStateCard() {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("📂", style = MaterialTheme.typography.displayMedium)
            Text("No projects yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Tap \"New Project\" to get started", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatBudget(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply { minimumFractionDigits = 0; maximumFractionDigits = 0 }.format(amount)
