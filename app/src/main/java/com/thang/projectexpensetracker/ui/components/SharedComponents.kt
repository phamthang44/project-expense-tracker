package com.thang.projectexpensetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.OutlinedTextFieldDefaults
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
        contentAlignment = Alignment.Center
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = BottomNavItem.entries.toTypedArray()
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
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
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

// ─────────────────────────────────────────────────────────────────────────────
// Reusable: white card wrapper for Forms
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AddCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable: single form field with flat grey background (no outlined border)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AddFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    leadingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    trailingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    minLines: Int = 1,
    maxLines: Int = 1,
    singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) ErrorRed else LabelColor
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, color = HintColor, style = MaterialTheme.typography.bodyMedium)
            },
            leadingIcon = leadingIcon,
            isError = isError,
            singleLine = singleLine,
            minLines = if (!singleLine) minLines else 1,
            maxLines = if (!singleLine) maxLines else 1,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = FieldBg,
                focusedContainerColor   = FieldBg,
                errorContainerColor     = Color(0xFFFEF2F2),
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = VividBlue,
                errorBorderColor        = ErrorRed
            )
        )
        // Inline helpers row
        if (errorMessage != null || trailingText != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = ErrorRed,
                    style = MaterialTheme.typography.labelSmall
                )
                if (trailingText != null) {
                    Text(
                        text = trailingText,
                        color = HintColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section label — bold title with vivid blue left accent bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FormSectionLabel(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(VividBlue)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = LabelColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date picker button — shared component
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FormDateButton(
    label: String,
    isPlaceholder: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isError) Color(0xFFFEF2F2) else FieldBg,
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
                tint = if (isError) ErrorRed else HintColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (isPlaceholder) HintColor else LabelColor
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STACKED AVATARS  (overlapping avatar circles for project card member count)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StackedAvatars(
    count: Int,
    modifier: Modifier = Modifier
) {
    val visible  = minOf(count, 2)
    val overflow = count - visible

    Box(modifier = modifier.height(26.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            repeat(visible) { idx ->
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .border(2.dp, AddCardBg, CircleShape)
                        .background(AvatarColors[idx % AvatarColors.size]),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = LabelColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            if (overflow > 0) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .border(2.dp, AddCardBg, CircleShape)
                        .background(ProgressTrack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$overflow",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = LabelColor
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// expOutlinedFieldColors — shared OutlinedTextField color scheme for forms.
// Single Responsibility: field color mapping only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun expOutlinedFieldColors(isError: Boolean) = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor    = if (isError) ErrorRed else InputBorder,
    focusedBorderColor      = if (isError) ErrorRed else VividBlue,
    unfocusedContainerColor = InputFieldBg,
    focusedContainerColor   = AddCardBg,
    errorBorderColor        = ErrorRed
)

// ─────────────────────────────────────────────────────────────────────────────
// ExpFormLabel — labelled form field header with optional required/optional markers.
// Single Responsibility: label rendering only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ExpFormLabel(
    label: String,
    required: Boolean = false,
    optional: Boolean = false,
    error: String? = null
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = if (error != null) ErrorRed else LabelColor
        )
        if (required) Text("*", color = ErrorRed, fontWeight = FontWeight.Bold)
        if (optional) Text("(Optional)", style = MaterialTheme.typography.bodySmall, color = HintColor)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ExpErrorText — inline validation error message below a field.
// Single Responsibility: error text rendering only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ExpErrorText(msg: String) {
    Text(msg, style = MaterialTheme.typography.labelSmall, color = ErrorRed)
}

// ─────────────────────────────────────────────────────────────────────────────
// ExpLockedField — read-only auto-generated ID display field.
// Single Responsibility: locked value display only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ExpLockedField(value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(InputFieldBg)
            .border(1.dp, InputBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(value, style = MaterialTheme.typography.bodyMedium, color = HintColor)
        Icon(Icons.Default.Lock, contentDescription = null, tint = HintColor, modifier = Modifier.size(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ToggleButton — single selectable pill button.
// Single Responsibility: selected/unselected toggle rendering only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ToggleButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isActive) VividBlue else InputBorder
    val bgColor     = if (isActive) ActiveTintBg else AddCardBg
    val textColor   = if (isActive) VividBlue else LabelColor

    Box(
        modifier         = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color      = textColor,
            textAlign  = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ToggleButtonRow — single-row toggle button group (e.g. Payment Status).
// Single Responsibility: horizontal toggle layout only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ToggleButtonRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            ToggleButton(
                label    = option,
                isActive = selected == option,
                onClick  = { onSelect(option) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ToggleButtonGrid — multi-row toggle button group (e.g. Payment Method).
// Single Responsibility: grid toggle layout only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ToggleButtonGrid(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    columns: Int = 2
) {
    val rows = (options.size + columns - 1) / columns
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(rows) { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(columns) { col ->
                    val idx    = row * columns + col
                    val option = options.getOrNull(idx)
                    if (option != null) {
                        ToggleButton(
                            label    = option,
                            isActive = selected == option,
                            onClick  = { onSelect(option) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

