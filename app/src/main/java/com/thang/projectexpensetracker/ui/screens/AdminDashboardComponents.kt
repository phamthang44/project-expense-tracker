package com.thang.projectexpensetracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import com.thang.projectexpensetracker.ui.components.StackedAvatars
import com.thang.projectexpensetracker.ui.components.StatusTag
import com.thang.projectexpensetracker.ui.theme.*
import com.thang.projectexpensetracker.util.FormatUtils

// Filter tab labels — screen-scoped constant, no UI logic
internal val filterTabs = listOf("All", "Active", "Completed", "On Hold")

// ─────────────────────────────────────────────────────────────────────────────
// DashboardTopBar — app bar with menu / search / profile actions
// Single Responsibility: top navigation UI only.
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Projects",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            IconButton(onClick = onProfileClick) {
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
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor    = AddPageBg,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// DashboardSearchPill — tappable search shortcut bar
// Single Responsibility: search entry point UI only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun DashboardSearchPill(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape           = RoundedCornerShape(50),
        color           = Color(0xFFEEF0F5),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = HintColor, modifier = Modifier.size(18.dp))
            Text("Search projects...", style = MaterialTheme.typography.bodyMedium, color = HintColor)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DashboardFilterTabs — status filter tab row
// Single Responsibility: tab UI and selected-tab rendering only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun DashboardFilterTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = filterTabs.indexOf(selectedTab),
        edgePadding      = 12.dp,
        containerColor   = Color.Transparent,
        contentColor     = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            val index = filterTabs.indexOf(selectedTab)
            if (index in tabPositions.indices) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.BottomStart)
                        .offset(x = tabPositions[index].left)
                        .width(tabPositions[index].width)
                        .height(3.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                        )
                )
            }
        },
        divider = {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    ) {
        filterTabs.forEach { tab ->
            val selected = selectedTab == tab
            Tab(
                selected = selected,
                onClick  = { onTabSelected(tab) },
                text = {
                    Text(
                        text       = tab,
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color      = if (selected) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DashboardProjectList — scrollable project card feed
// Single Responsibility: LazyColumn layout + empty state delegation only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun DashboardProjectList(
    filteredProjects: List<ProjectEntity>,
    expenseTotals: Map<Long, Double>,
    selectedTab: String,
    onProjectClick: (Long) -> Unit
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (filteredProjects.isEmpty()) {
            item { EmptyDashboardState(selectedTab) }
        } else {
            items(filteredProjects, key = { it.id }) { project ->
                PolishedProjectCard(
                    project    = project,
                    usedBudget = expenseTotals[project.id] ?: 0.0,
                    onClick    = { onProjectClick(project.id) }
                )
            }
        }
        item { Spacer(Modifier.height(88.dp)) }   // clear FAB
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PolishedProjectCard — renders one project's data as a card
// Single Responsibility: project card UI only.
// Progress color → budgetProgressColor() in Color.kt
// Budget formatting → FormatUtils.formatBudget()
// Avatar widget → StackedAvatars in SharedComponents
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun PolishedProjectCard(
    project: ProjectEntity,
    usedBudget: Double,
    onClick: () -> Unit
) {
    val fraction      = if (project.budget > 0) (usedBudget / project.budget).toFloat().coerceIn(0f, 1f) else 0f
    val progressColor = budgetProgressColor(fraction, project.status)

    val animatedFraction by animateFloatAsState(
        targetValue   = fraction,
        animationSpec = tween(durationMillis = 900),
        label         = "progress"
    )

    // Deterministic member-count avatar (1–4) seeded from project id
    val memberCount = ((project.id % 4) + 1).toInt()

    Card(
        onClick   = onClick,
        modifier  = Modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 4.dp,
                shape        = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor    = Color.Black.copy(alpha = 0.12f)
            ),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = AddCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // ── Row 1: Project name + Status badge ───────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = project.projectName,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f).padding(end = 10.dp)
                )
                StatusTag(status = project.status)
            }

            // ── Row 2: Manager ───────────────────────────────────────────────
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Person, contentDescription = null, tint = HintColor, modifier = Modifier.size(13.dp))
                Text(text = project.manager, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Spacer(Modifier.height(14.dp))

            // ── Budget label + amounts ────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Budget Usage", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = LabelColor)
                Text(
                    text       = "$${FormatUtils.formatBudget(usedBudget)} / $${FormatUtils.formatBudget(project.budget)}",
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF111827)
                )
            }

            // ── Progress Bar ──────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(ProgressTrack)) {
                Box(modifier = Modifier.fillMaxWidth(animatedFraction).fillMaxHeight().clip(RoundedCornerShape(50)).background(progressColor))
            }

            Spacer(Modifier.height(14.dp))

            // ── Row 3: Start date + Avatar stack ─────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = HintColor, modifier = Modifier.size(13.dp))
                    Text(text = "Start: ${project.startDate}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                }
                StackedAvatars(count = memberCount)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun EmptyDashboardState(filter: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("📂", style = MaterialTheme.typography.displaySmall)
        Text(
            text = "No $filter projects",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Text(
            text = "Tap + to create your first project",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9CA3AF)
        )
    }
}
