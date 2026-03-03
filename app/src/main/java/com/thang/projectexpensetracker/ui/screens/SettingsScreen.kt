package com.thang.projectexpensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// Settings Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // State for toggles (UI-only, not persisted)
    var selectedTheme    by remember { mutableStateOf("light") }   // light / dark / system
    var syncAlerts       by remember { mutableStateOf(true) }
    var budgetReminders  by remember { mutableStateOf(false) }
    var syncFrequency    by remember { mutableStateOf("Auto") }    // Auto / Daily / Manual

    var showComingSoon   by remember { mutableStateOf(false) }

    // "Coming Soon" Snackbar
    if (showComingSoon) {
        AlertDialog(
            onDismissRequest = { showComingSoon = false },
            icon  = { Icon(Icons.Default.Build, null, tint = SetBlue) },
            title = { Text("Coming Soon") },
            text  = { Text("This feature will be implemented in a future update. Stay tuned!") },
            confirmButton = {
                TextButton(onClick = { showComingSoon = false }) { Text("OK") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = SetBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = SetDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showComingSoon = true }) {
                        Icon(Icons.Default.HelpOutline, "Help", tint = SetGrey)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SetBg)
            )
        },
        bottomBar = {
            com.thang.projectexpensetracker.ui.components.AppBottomNavigationBar(
                currentRoute = "settings",
                onNavigate = onNavigate
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

            // ═══════════════════════════════════════════════════════════════
            // PROFILE CARD
            // ═══════════════════════════════════════════════════════════════
            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = SetLightBlue),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Avatar circle
                    Box(
                        modifier         = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SetBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint     = SetBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    // Name & email
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Alex Johnson",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = SetDark
                        )
                        Text(
                            "alex.j@project-sync.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = SetGrey
                        )
                        Spacer(Modifier.height(4.dp))
                        // PREMIUM badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SetBlue)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "✦ PREMIUM PLAN",
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White,
                                fontSize   = 10.sp
                            )
                        }
                    }
                    // Edit icon
                    IconButton(onClick = { showComingSoon = true }) {
                        Icon(Icons.Default.Edit, "Edit profile", tint = SetBlue)
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // APPEARANCE
            // ═══════════════════════════════════════════════════════════════
            SectionLabel("APPEARANCE")

            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = SetCard),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column {
                    ThemeOption(
                        title       = "Light",
                        subtitle    = "Crisp and clean interface",
                        icon        = Icons.Default.WbSunny,
                        isSelected  = selectedTheme == "light",
                        onClick     = { selectedTheme = "light" }
                    )
                    HorizontalDivider(color = SetBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    ThemeOption(
                        title       = "Dark",
                        subtitle    = "Reduced glare for night use",
                        icon        = Icons.Default.DarkMode,
                        isSelected  = selectedTheme == "dark",
                        onClick     = { selectedTheme = "dark" }
                    )
                    HorizontalDivider(color = SetBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    ThemeOption(
                        title       = "System Default",
                        subtitle    = "Follow device preferences",
                        icon        = Icons.Default.Devices,
                        isSelected  = selectedTheme == "system",
                        onClick     = { selectedTheme = "system" }
                    )
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // NOTIFICATIONS
            // ═══════════════════════════════════════════════════════════════
            SectionLabel("NOTIFICATIONS")

            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = SetCard),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsToggleRow(
                        title    = "Sync Alerts",
                        subtitle = "Notify when cloud data updates",
                        checked  = syncAlerts,
                        onToggle = { syncAlerts = it }
                    )
                    HorizontalDivider(color = SetBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsToggleRow(
                        title    = "Budget Reminders",
                        subtitle = "Alert when nearing category limits",
                        checked  = budgetReminders,
                        onToggle = { budgetReminders = it }
                    )
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // DATA MANAGEMENT
            // ═══════════════════════════════════════════════════════════════
            SectionLabel("DATA MANAGEMENT")

            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = SetCard),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column {
                    ActionRow(
                        icon      = Icons.Default.Cached,
                        title     = "Clear Local Cache",
                        trailing  = "24.5 MB",
                        onClick   = { showComingSoon = true }
                    )
                    HorizontalDivider(color = SetBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    ActionRow(
                        icon      = Icons.Default.FileDownload,
                        title     = "Export Data (CSV/PDF)",
                        showArrow = true,
                        onClick   = { showComingSoon = true }
                    )
                    HorizontalDivider(color = SetBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    ActionRow(
                        icon      = Icons.Default.Backup,
                        title     = "Database Backup",
                        showArrow = true,
                        onClick   = { showComingSoon = true }
                    )
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // ACCOUNT & SYNC
            // ═══════════════════════════════════════════════════════════════
            SectionLabel("ACCOUNT & SYNC")

            // Cloud Sync Frequency
            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = SetCard),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Cloud Sync Frequency",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = SetDark
                    )
                    // Segmented button row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, SetBorder, RoundedCornerShape(10.dp))
                    ) {
                        listOf("Auto", "Daily", "Manual").forEach { option ->
                            val isActive = syncFrequency == option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isActive) SetBlue else Color.Transparent)
                                    .clickable { syncFrequency = option }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    option,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (isActive) Color.White else SetGrey
                                )
                            }
                        }
                    }
                }
            }

            // Log Out button
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showComingSoon = true }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    null,
                    tint     = Color(0xFFDC2626),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Log Out",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFFDC2626)
                )
            }

            // ═══════════════════════════════════════════════════════════════
            // FOOTER
            // ═══════════════════════════════════════════════════════════════
            Spacer(Modifier.height(8.dp))

            Column(
                modifier            = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // App icon
                Box(
                    modifier         = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SetLightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        null,
                        tint     = SetBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "ExpensePro Tracker",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = SetDark
                )
                Text(
                    "Version 2.4.1 (Build 829)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SetGrey
                )

                Spacer(Modifier.height(8.dp))

                // Footer links
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showComingSoon = true }) {
                        Text(
                            "Privacy\nPolicy",
                            style     = MaterialTheme.typography.labelSmall,
                            color     = SetBlue,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                    Text("•", color = SetGrey, fontSize = 10.sp)
                    TextButton(onClick = { showComingSoon = true }) {
                        Text(
                            "Terms of\nService",
                            style     = MaterialTheme.typography.labelSmall,
                            color     = SetBlue,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                    Text("•", color = SetGrey, fontSize = 10.sp)
                    TextButton(onClick = { showComingSoon = true }) {
                        Text(
                            "Contact\nSupport",
                            style     = MaterialTheme.typography.labelSmall,
                            color     = SetBlue,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Future features note
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                        Text(
                            "All features on this page are placeholders and will be implemented in future updates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
