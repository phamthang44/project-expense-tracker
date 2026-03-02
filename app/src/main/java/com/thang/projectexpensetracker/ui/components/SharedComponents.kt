package com.thang.projectexpensetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// STATUS TAG  (Project status: Active, On Hold, Completed, Cancelled)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StatusTag(
    status: String,
    modifier: Modifier = Modifier
) {
    val (container, content) = when (status.lowercase().trim()) {
        "active"              -> StatusActiveContainer    to StatusActiveContent
        "completed"           -> StatusCompletedContainer to StatusCompletedContent
        "on hold", "pending"  -> StatusOnHoldContainer    to StatusOnHoldContent
        else                  -> StatusCancelledContainer to StatusCancelledContent  // Cancelled / Rejected
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = container
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = "Status: $status",
                tint = content,
                modifier = Modifier.size(6.dp)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = content
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PRIORITY TAG
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PriorityTag(
    priority: String,
    modifier: Modifier = Modifier
) {
    val (container, content) = when (priority.lowercase().trim()) {
        "high"   -> PriorityHighContainer   to PriorityHighContent
        "low"    -> PriorityLowContainer    to PriorityLowContent
        else     -> PriorityNormalContainer to PriorityNormalContent
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = container
    ) {
        Text(
            text = priority,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = content
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAYMENT STATUS TAG  (Paid, Pending, Reimbursed)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PaymentStatusTag(
    status: String,
    modifier: Modifier = Modifier
) {
    val (container, content) = when (status.lowercase().trim()) {
        "paid"        -> PaidContainer       to PaidContent        // Purple
        "reimbursed"  -> ReimbursedContainer to ReimbursedContent  // Blue
        else          -> PendingContainer    to PendingContent     // Deep Orange
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = container
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = "Payment status: $status",
                tint = content,
                modifier = Modifier.size(6.dp)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = content
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FORM SECTION HEADER  (modern: vertical accent bar + label pill)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FormSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Vertical accent bar
        Surface(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp),
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.primary
        ) {}

        // Section label
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = title.uppercase(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                letterSpacing = 0.8.sp
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// INFO ROW  (label + value pair for detail / confirmation screens)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.58f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM NAVIGATION
// ─────────────────────────────────────────────────────────────────────────────
enum class BottomNavItem(val label: String, val icon: ImageVector, val route: String) {
    Projects("Projects", Icons.Default.Folder, "admin_dashboard"),
    Sync("Sync", Icons.Default.SyncAlt, "sync"),
    Settings("Settings", Icons.Default.Settings, "settings")
}


@Composable
fun AppBottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    // Floating glassy container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp), // Floating offset
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Slight inset
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = CircleShape
                )
                // Using shadow/elevation on the row itself
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            val items = BottomNavItem.values()
            items.forEach { item ->
                val selected = currentRoute == item.route
                val contentColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(300),
                    label = "navColor"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                    animationSpec = tween(300),
                    label = "navBg"
                )

                Box(
                    modifier = Modifier
                        .background(color = bgColor, shape = CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (!selected) onNavigate(item.route)
                            }
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = contentColor,
                            modifier = Modifier.size(26.dp)
                        )
                        if (selected) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
