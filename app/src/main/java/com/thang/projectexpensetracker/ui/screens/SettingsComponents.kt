package com.thang.projectexpensetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Design tokens (shared with SettingsScreen) ──────────────────────────────
internal val SetBg         = Color(0xFFF2F4F7)
internal val SetCard       = Color(0xFFFFFFFF)
internal val SetBlue       = Color(0xFF2563EB)
internal val SetDark       = Color(0xFF111827)
internal val SetGrey       = Color(0xFF6B7280)
internal val SetBorder     = Color(0xFFE5E7EB)
internal val SetLightBlue  = Color(0xFFEFF6FF)
internal val SetSectionBlue = Color(0xFF3B82F6)

// ─────────────────────────────────────────────────────────────────────────────
// Section label (blue uppercase text)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun SectionLabel(text: String) {
    Text(
        text,
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color      = SetSectionBlue,
        letterSpacing = 0.8.sp,
        modifier   = Modifier.padding(start = 4.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Theme option row (radio + icon)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun ThemeOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick  = onClick,
            colors   = RadioButtonDefaults.colors(
                selectedColor   = SetBlue,
                unselectedColor = SetGrey
            )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = SetDark
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SetGrey
            )
        }
        Icon(icon, null, tint = if (isSelected) SetBlue else SetGrey, modifier = Modifier.size(22.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Toggle/switch row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = SetDark
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SetGrey
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SetBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = SetBorder
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Action row (icon + title + trailing text or arrow)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun ActionRow(
    icon: ImageVector,
    title: String,
    trailing: String? = null,
    showArrow: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = SetGrey, modifier = Modifier.size(22.dp))
        Text(
            title,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color      = SetDark,
            modifier   = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.bodySmall, color = SetGrey)
        }
        if (showArrow) {
            Icon(Icons.Default.ChevronRight, null, tint = SetGrey, modifier = Modifier.size(20.dp))
        }
    }
}
