package com.thang.projectexpensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.model.SyncStatus
import kotlinx.coroutines.delay

// ─── Token Colors for Sync Screen ────────────────────────────────────────────
private val SyBg      = Color(0xFFF8FAFC)
private val SyCard    = Color(0xFFFFFFFF)
private val SyBlue    = Color(0xFF2563EB)
private val SyDark    = Color(0xFF0F172A)
private val SyGrey    = Color(0xFF64748B)
private val SyBorder  = Color(0xFFE2E8F0)

private val SyGreenBg = Color(0xFFDCFCE7)
private val SyGreen   = Color(0xFF16A34A)

private val SyLightBlueBg = Color(0xFFEFF6FF)
private val SyLightBlue   = Color(0xFF3B82F6)

private val SyOrangeBg = Color(0xFFFEF3C7)
private val SyOrange   = Color(0xFFD97706)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    syncStatus: SyncStatus,
    lastSyncTime: String?,
    pendingCount: Int,
    isOffline: Boolean,
    onSyncNow: () -> Unit,
    onToggleOffline: () -> Unit,
    onDismissError: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    var showComingSoon by remember { mutableStateOf(false) }

    // Fake progress animation for UX
    var progress by remember { mutableFloatStateOf(0f) }
    var estimatedTime by remember { mutableIntStateOf(12) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progressAnim"
    )

    LaunchedEffect(syncStatus) {
        if (syncStatus == SyncStatus.SYNCING) {
            progress = 0f
            estimatedTime = 12
            // Simulate progress filling up to ~95%
            while (progress < 0.95f) {
                delay(400)
                progress += (1f - progress) * 0.2f
                if (estimatedTime > 1) estimatedTime--
            }
        } else if (syncStatus == SyncStatus.SUCCESS || syncStatus == SyncStatus.ERROR) {
            progress = if (syncStatus == SyncStatus.SUCCESS) 1f else 0f
        } else if (syncStatus == SyncStatus.IDLE) {
            progress = 0f
        }
    }

    if (showComingSoon) {
        AlertDialog(
            onDismissRequest = { showComingSoon = false },
            icon  = { Icon(Icons.Default.Build, null, tint = SyBlue) },
            title = { Text("Coming Soon") },
            text  = { Text("This feature will be implemented in a future update.") },
            confirmButton = {
                TextButton(onClick = { showComingSoon = false }) { Text("OK") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = SyBg,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Cloud Synchronization",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SyDark,
                            modifier = Modifier.offset(x = (-24).dp) // Center horizontally balancing back icon
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SyDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SyCard)
            )
        },
        bottomBar = {
            com.thang.projectexpensetracker.ui.components.AppBottomNavigationBar(
                currentRoute = "sync",
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
            // CONNECTED CARD
            // ═══════════════════════════════════════════════════════════════
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SyCard),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cloud Icon
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SyGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudDone, null, tint = SyGreen, modifier = Modifier.size(24.dp))
                        // Little green dot badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SyGreen)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Connected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SyDark)
                        Text("Encryption: AES-256", style = MaterialTheme.typography.bodySmall, color = SyGrey)
                        Text("Active", style = MaterialTheme.typography.bodySmall, color = SyGrey)
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // SYNCING PROGRESS CARD
            // ═══════════════════════════════════════════════════════════════
            AnimatedVisibility(visible = syncStatus == SyncStatus.SYNCING || progress > 0f) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SyCard),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Syncing Progress", style = MaterialTheme.typography.bodySmall, color = SyGrey)
                                Text("${(animatedProgress * 100).toInt()}% Complete", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SyDark)
                            }
                            Text(
                                "Estimated: $estimatedTime seconds",
                                style = MaterialTheme.typography.labelSmall,
                                color = SyGrey,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        // Progress bar
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = SyBlue,
                            trackColor = SyLightBlueBg
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = SyGrey, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Uploading local data to secure vault...", style = MaterialTheme.typography.bodySmall, color = SyGrey)
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // LAST SYNCED & PENDING GRIDS
            // ═══════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Last Synced
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SyCard),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.weight(1f).clickable { showComingSoon = true }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.History, null, tint = SyGrey, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Last Synced", style = MaterialTheme.typography.bodySmall, color = SyGrey)
                        Text(
                            "Not Available", // Replaced hardcoded "2 hours ago"
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SyDark
                        )
                    }
                }

                // Pending
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SyCard),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.weight(1f).clickable { showComingSoon = true }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.UploadFile, null, tint = SyGrey, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Pending", style = MaterialTheme.typography.bodySmall, color = SyGrey)
                        Text(
                            "Not Available", // Hidden pending items behind future build
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SyDark
                        )
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // SYNC NOW BUTTON
            // ═══════════════════════════════════════════════════════════════
            Button(
                onClick = onSyncNow,
                enabled = syncStatus != SyncStatus.SYNCING && !isOffline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SyBlue)
            ) {
                if (syncStatus == SyncStatus.SYNCING) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Sync, null, modifier = Modifier.size(20.dp), tint = Color.White)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    if (syncStatus == SyncStatus.SYNCING) "Syncing..." else "Sync Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // ═══════════════════════════════════════════════════════════════
            // AUTO SYNC & LOG ROWS
            // ═══════════════════════════════════════════════════════════════
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SyCard),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Auto-sync
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showComingSoon = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Autorenew, null, tint = SyGrey, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Auto-sync", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SyDark)
                                Text("Sync changes automatically", style = MaterialTheme.typography.bodySmall, color = SyGrey)
                            }
                        }
                        Switch(
                            checked = true, // Visual only
                            onCheckedChange = { showComingSoon = true },
                            colors = SwitchDefaults.colors(checkedTrackColor = SyBlue)
                        )
                    }
                    
                    HorizontalDivider(color = SyBorder)
                    
                    // View Sync Log
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showComingSoon = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ListAlt, null, tint = SyGrey, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("View Sync Log", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SyDark)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = SyGrey, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // RECENT ACTIVITY LIST (Placeholders)
            // ═══════════════════════════════════════════════════════════════
            Text(
                "RECENT ACTIVITY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SyGrey,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SyCard),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth().clickable { showComingSoon = true }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(SyLightBlueBg), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Architecture, null, tint = SyLightBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Project structure synced", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SyDark)
                        Text("Future feature • Data", style = MaterialTheme.typography.bodySmall, color = SyGrey)
                    }
                    Icon(Icons.Default.CheckCircle, null, tint = SyGreen, modifier = Modifier.size(16.dp))
                }
            }
            
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SyCard),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth().clickable { showComingSoon = true }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(SyOrangeBg), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FlightTakeoff, null, tint = SyOrange, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Expenses batch uploaded", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SyDark)
                        Text("Future feature • Images", style = MaterialTheme.typography.bodySmall, color = SyGrey)
                    }
                    Icon(Icons.Default.CheckCircle, null, tint = SyGreen, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
