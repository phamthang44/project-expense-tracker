package com.thang.projectexpensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import com.thang.projectexpensetracker.ui.components.AppBottomNavigationBar
import com.thang.projectexpensetracker.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// AdminDashboardScreen — screen orchestrator
// Single Responsibility: owns filter state, composes sub-composables only.
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
        containerColor = AddPageBg,
        topBar = {
            DashboardTopBar(
                onMenuClick    = onMenuClick,
                onSearchClick  = onSearchClick,
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            AppBottomNavigationBar(
                currentRoute = "admin_dashboard",
                onNavigate   = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onAddClick,
                shape          = RoundedCornerShape(16.dp),
                containerColor = VividBlue,
                elevation      = FloatingActionButtonDefaults.elevation(
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
            DashboardSearchPill(onSearchClick)
            DashboardFilterTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            DashboardProjectList(
                filteredProjects = filteredProjects,
                expenseTotals    = expenseTotals,
                selectedTab      = selectedTab,
                onProjectClick   = onProjectClick
            )
        }
    }
}
