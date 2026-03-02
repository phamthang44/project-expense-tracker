package com.thang.projectexpensetracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import com.thang.projectexpensetracker.ui.components.StatusTag
import java.text.NumberFormat
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Design tokens — match the design image palette
// ─────────────────────────────────────────────────────────────────────────────
private val PageBackground  = Color(0xFFF2F4F7)   // soft grey page tint
private val CardBackground  = Color(0xFFFFFFFF)   // pure white cards
private val ProgressBlue    = Color(0xFF2563EB)   // vivid blue (Active)
private val ProgressOrange  = Color(0xFFF59E0B)   // amber-orange (On Hold)
private val ProgressRed     = Color(0xFFDC2626)   // red (over-budget)
private val ProgressTrack   = Color(0xFFE5E7EB)   // light grey track

// Avatar accent colours — match the design image circles
private val AvatarColors = listOf(
    Color(0xFFBFDBFE), // light blue
    Color(0xFFBBF7D0), // light green
    Color(0xFFFDE68A), // light yellow
    Color(0xFFFBCFE8), // light pink
    Color(0xFFDDD6FE), // light purple
)

// ─────────────────────────────────────────────────────────────────────────────
// Bottom nav destinations
// ─────────────────────────────────────────────────────────────────────────────

private val filterTabs = listOf("All", "Active", "Completed", "On Hold")

// ─────────────────────────────────────────────────────────────────────────────
// AdminDashboardScreen — the Home Page
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    projects: List<ProjectEntity>,
    expenseTotals: Map<Long, Double> = emptyMap(),
    onAddClick: () -> Unit = {},
    onProjectClick: (Long) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("All") }

    val filteredProjects = remember(projects, selectedTab) {
        projects.filter { p ->
            when (selectedTab) {
                "Active"    -> p.status.equals("Active",    ignoreCase = true)
                "Completed" -> p.status.equals("Completed", ignoreCase = true)
                "On Hold"   -> p.status.equals("On Hold",  ignoreCase = true) ||
                               p.status.equals("Pending",  ignoreCase = true)
                else        -> true
            }
        }
    }

    Scaffold(
        containerColor = PageBackground,
        // ── Top App Bar ──────────────────────────────────────────────────────
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Projects",
                        style = MaterialTheme.typography.titleLarge,
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
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PageBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        // ── Bottom Nav ───────────────────────────────────────────────────────
        bottomBar = {
            com.thang.projectexpensetracker.ui.components.AppBottomNavigationBar(
                currentRoute = "admin_dashboard",
                onNavigate = onNavigate
            )
        },
        // ── FAB ─────────────────────────────────────────────────────────────
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = RoundedCornerShape(16.dp),
                containerColor = Color(0xFF2563EB),   // vivid blue — matches design image
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Project", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Search Pill ──────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onSearchClick() },
                shape = RoundedCornerShape(50),          // pill shape
                color = Color(0xFFEEF0F5),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Search projects...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            // ── Filter Tabs ──────────────────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = filterTabs.indexOf(selectedTab),
                edgePadding = 12.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
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
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            ) {
                filterTabs.forEach { tab ->
                    val selected = selectedTab == tab
                    Tab(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // ── Project List ─────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (filteredProjects.isEmpty()) {
                    item { EmptyDashboardState(selectedTab) }
                } else {
                    items(filteredProjects, key = { it.id }) { project ->
                        PolishedProjectCard(
                            project       = project,
                            usedBudget    = expenseTotals[project.id] ?: 0.0,
                            onClick       = { onProjectClick(project.id) }
                        )
                    }
                }
                item { Spacer(Modifier.height(88.dp)) }   // clear FAB
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Polished Project Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PolishedProjectCard(
    project: ProjectEntity,
    usedBudget: Double,
    onClick: () -> Unit
) {
    val fraction = if (project.budget > 0)
        (usedBudget / project.budget).toFloat().coerceIn(0f, 1f) else 0f

    val progressColor = when {
        fraction >= 0.90f -> ProgressRed
        fraction >= 0.70f -> ProgressOrange
        else              -> when (project.status.lowercase()) {
            "completed" -> ProgressBlue
            "on hold", "pending" -> ProgressOrange
            else -> ProgressBlue
        }
    }

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 900),
        label = "progress"
    )

    // Member count (1-4, seeded from project id)
    val memberCount = ((project.id % 4) + 1).toInt()

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor   = Color.Black.copy(alpha = 0.12f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)   // shadow via Modifier
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // ── Row 1: Project name + Status badge ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.projectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 10.dp)
                )
                StatusTag(status = project.status)
            }

            // ── Row 2: Manager ───────────────────────────────────────────────
            Spacer(Modifier.height(5.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = project.manager,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Budget label + amounts ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Budget Usage",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Text(
                    "$${dashFmt(usedBudget)} / $${dashFmt(project.budget)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
            }

            // ── Progress Bar ─────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ProgressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(progressColor)
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Row 3: Start date + Avatar stack ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Start: ${project.startDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }

                StackedAvatars(count = memberCount)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stacked Avatars   (white border ring for the "stack" look in the screenshot)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StackedAvatars(count: Int) {
    val visible  = minOf(count, 2)
    val overflow = count - visible

    // Each avatar is offset left so they overlap. We build them RTL.
    Box(modifier = Modifier.height(26.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            repeat(visible) { idx ->
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .border(2.dp, CardBackground, CircleShape)  // white ring
                        .background(AvatarColors[idx % AvatarColors.size]),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF374151),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            if (overflow > 0) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .border(2.dp, CardBackground, CircleShape)
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "+$overflow",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EmptyDashboardState(filter: String) {
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

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun dashFmt(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US)
        .apply { minimumFractionDigits = 0; maximumFractionDigits = 0 }
        .format(amount)

