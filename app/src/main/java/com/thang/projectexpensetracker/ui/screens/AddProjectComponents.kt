package com.thang.projectexpensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thang.projectexpensetracker.model.ProjectFormState
import com.thang.projectexpensetracker.ui.components.AddFormField
import com.thang.projectexpensetracker.ui.components.FormCard
import com.thang.projectexpensetracker.ui.components.FormDateButton
import com.thang.projectexpensetracker.ui.components.FormSectionLabel
import com.thang.projectexpensetracker.ui.theme.*
import com.thang.projectexpensetracker.util.DateUtils

@Composable
internal fun BasicInfoSection(
    state: ProjectFormState,
    onUpdate: (ProjectFormState) -> Unit
) {
    FormSectionLabel("Basic Information")
    FormCard {
        AddFormField(
            value = state.projectName,
            onValueChange = {
                if (it.length <= 80) onUpdate(state.copy(projectName = it, nameError = if (it.isNotBlank()) null else state.nameError))
            },
            label = "Project Name *",
            placeholder = "e.g. Q4 Marketing Campaign",
            isError = state.nameError != null,
            errorMessage = state.nameError,
            trailingText = "${state.projectName.length}/80",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        AddFormField(
            value = state.description,
            onValueChange = {
                if (it.length <= 300) onUpdate(state.copy(description = it, descError = if (it.isNotBlank()) null else state.descError))
            },
            label = "Project Description *",
            placeholder = "Describe project goals and scope...",
            isError = state.descError != null,
            errorMessage = state.descError,
            trailingText = "${state.description.length}/300",
            minLines = 2,
            maxLines = 3,
            singleLine = false,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleSection(
    state: ProjectFormState,
    onUpdate: (ProjectFormState) -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    // Date formatting delegated to DateUtils — no formatting logic in the UI layer.

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { ms ->
                        onUpdate(state.copy(startDateStr = DateUtils.millisToDisplayDate(ms), startDateError = null))
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = startDatePickerState) }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let { ms ->
                        onUpdate(state.copy(endDateStr = DateUtils.millisToDisplayDate(ms), endDateError = null))
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = endDatePickerState) }
    }

    FormSectionLabel("Schedule")
    FormCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Start Date *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.startDateError != null) ErrorRed else LabelColor
                )
                FormDateButton(
                    label = state.startDateStr.ifBlank { "dd/MM/yyyy" },
                    isPlaceholder = state.startDateStr.isBlank(),
                    isError = state.startDateError != null,
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.startDateError != null) {
                    Text(
                        state.startDateError,
                        color = ErrorRed,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "End Date *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.endDateError != null) ErrorRed else LabelColor
                )
                FormDateButton(
                    label = state.endDateStr.ifBlank { "dd/MM/yyyy" },
                    isPlaceholder = state.endDateStr.isBlank(),
                    isError = state.endDateError != null,
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.endDateError != null) {
                    Text(
                        state.endDateError,
                        color = ErrorRed,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProjectDetailsSection(
    state: ProjectFormState,
    onUpdate: (ProjectFormState) -> Unit
) {
    val statusOptions = listOf("Active", "On Hold", "Completed", "Cancelled")
    var isStatusExpanded by remember { mutableStateOf(false) }
    val priorityOptions = listOf("Low", "Normal", "High")

    FormSectionLabel("Project Details")
    FormCard {
        AddFormField(
            value = state.manager,
            onValueChange = { onUpdate(state.copy(manager = it, managerError = if (it.isNotBlank()) null else state.managerError)) },
            label = "Project Manager *",
            placeholder = "Full name",
            leadingIcon = { Icon(Icons.Default.Person, null, tint = HintColor, modifier = Modifier.size(18.dp)) },
            isError = state.managerError != null,
            errorMessage = state.managerError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        )
    }

    FormCard {
        Text("Status *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = LabelColor)
        ExposedDropdownMenuBox(
            expanded = isStatusExpanded,
            onExpandedChange = { isStatusExpanded = it }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp),
                color = FieldBg
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Circle, null, tint = statusColor(state.selectedStatus), modifier = Modifier.size(10.dp))
                        Text(state.selectedStatus, style = MaterialTheme.typography.bodyMedium, color = LabelColor)
                    }
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = HintColor)
                }
            }
            ExposedDropdownMenu(
                expanded = isStatusExpanded,
                onDismissRequest = { isStatusExpanded = false }
            ) {
                statusOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        leadingIcon = { Icon(Icons.Default.Circle, null, Modifier.size(10.dp), tint = statusColor(option)) },
                        onClick = { onUpdate(state.copy(selectedStatus = option)); isStatusExpanded = false }
                    )
                }
            }
        }
    }

    FormCard {
        AddFormField(
            value = state.budgetStr,
            onValueChange = { onUpdate(state.copy(budgetStr = it, budgetError = null)) },
            label = "Budget *",
            placeholder = "0.00",
            isError = state.budgetError != null,
            errorMessage = state.budgetError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
        )
    }

    FormCard {
        Text("Priority Level *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = LabelColor)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            priorityOptions.forEach { option ->
                val selected = state.selectedPriority == option
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(50)).clickable { onUpdate(state.copy(selectedPriority = option)) },
                    shape = RoundedCornerShape(50),
                    color = if (selected) VividBlue else FieldBg
                ) {
                    Text(
                        option,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color.White else LabelColor
                    )
                }
            }
        }
    }
}

@Composable
internal fun AdditionalInfoSection(
    state: ProjectFormState,
    onUpdate: (ProjectFormState) -> Unit,
    showOptional: Boolean,
    onToggleShowOptional: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AddCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onToggleShowOptional(!showOptional)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Client / Department Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = LabelColor)
                Icon(if (showOptional) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = HintColor)
            }
            AnimatedVisibility(
                visible = showOptional,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AddFormField(
                        value = state.clientInfo,
                        onValueChange = { onUpdate(state.copy(clientInfo = it)) },
                        label = "Client / Department",
                        placeholder = "e.g. Marketing Dept / Acme Corp",
                        leadingIcon = { Icon(Icons.Default.Business, null, tint = HintColor, modifier = Modifier.size(18.dp)) }
                    )
                    AddFormField(
                        value = state.specialRequirements,
                        onValueChange = { onUpdate(state.copy(specialRequirements = it)) },
                        label = "Special Requirements (Optional)",
                        placeholder = "e.g. Projector, Wi-Fi, Catering...",
                        minLines = 2,
                        maxLines = 3,
                        singleLine = false
                    )
                }
            }
        }
    }
}

// statusColor() lives in ui/theme/Color.kt — no status-colour logic in the screen file.
