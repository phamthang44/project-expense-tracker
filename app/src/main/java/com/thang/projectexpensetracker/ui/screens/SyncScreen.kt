package com.thang.projectexpensetracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thang.projectexpensetracker.viewmodel.SyncStatus

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
    // Rotation animation for syncing spinner
    val infiniteTransition = rememberInfiniteTransition(label = "syncRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cloud Sync", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Offline Banner ─────────────────────────────────────
            if (isOffline) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.WifiOff, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("You are offline", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Changes will be synced when internet is restored.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // ── Sync Status Card ───────────────────────────────────
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedContent(targetState = syncStatus, label = "syncStatusIcon") { status ->
                        when (status) {
                            SyncStatus.SYNCING -> {
                                Icon(
                                    Icons.Default.Sync,
                                    contentDescription = "Syncing",
                                    modifier = Modifier.size(64.dp).rotate(rotation),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            SyncStatus.SUCCESS -> {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Sync successful",
                                    modifier = Modifier.size(64.dp),
                                    tint = androidx.compose.ui.graphics.Color(0xFF1A6B35)
                                )
                            }
                            SyncStatus.ERROR -> {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Sync error",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            SyncStatus.OFFLINE -> {
                                Icon(
                                    Icons.Default.WifiOff,
                                    contentDescription = "Offline",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            else -> {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = "Ready to sync",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        text = when (syncStatus) {
                            SyncStatus.SYNCING -> "Syncing your data…"
                            SyncStatus.SUCCESS -> "All data synced successfully!"
                            SyncStatus.ERROR   -> "Sync failed. Please try again."
                            SyncStatus.OFFLINE -> "No internet connection"
                            else               -> "Ready to sync"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (syncStatus == SyncStatus.SYNCING) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    Text(
                        text = when {
                            lastSyncTime != null -> "Last synced: $lastSyncTime"
                            else                 -> "Never synced"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Pending Items ──────────────────────────────────────
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Pending, null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Pending sync", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                            Text("Items awaiting upload", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Badge(containerColor = if (pendingCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            "$pendingCount",
                            color = if (pendingCount > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Sync Log / History ─────────────────────────────────
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Sync Log", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    SyncLogItem(icon = Icons.Default.CloudDone, label = "Projects", detail = if (syncStatus == SyncStatus.SUCCESS) "Synced" else "Pending")
                    SyncLogItem(icon = Icons.Default.Receipt, label = "Expenses", detail = if (syncStatus == SyncStatus.SUCCESS) "Synced" else "Pending")
                    SyncLogItem(icon = Icons.Default.Settings, label = "Settings", detail = "Up to date")
                }
            }

            // ── Error dismissal ────────────────────────────────────
            if (syncStatus == SyncStatus.ERROR) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sync Error", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Could not reach the server. Check your connection and retry.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(0.8f))
                        }
                        IconButton(onClick = onDismissError) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // ── Actions ────────────────────────────────────────────
            Button(
                onClick = onSyncNow,
                enabled = syncStatus != SyncStatus.SYNCING && !isOffline,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Sync, null)
                Spacer(Modifier.width(8.dp))
                Text("Sync Now", style = MaterialTheme.typography.labelLarge)
            }

            // Offline toggle (demo)
            OutlinedButton(
                onClick = onToggleOffline,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(if (isOffline) Icons.Default.Wifi else Icons.Default.WifiOff, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isOffline) "Go Online" else "Simulate Offline", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SyncLogItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
        Surface(shape = RoundedCornerShape(6.dp), color = if (detail == "Synced" || detail == "Up to date") androidx.compose.ui.graphics.Color(0xFFD4EDDA) else MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                detail,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (detail == "Synced" || detail == "Up to date") androidx.compose.ui.graphics.Color(0xFF1A6B35) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
