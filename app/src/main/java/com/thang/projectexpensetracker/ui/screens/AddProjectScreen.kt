package com.thang.projectexpensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ─── Design tokens (consistent with AdminDashboardScreen) ────────────────────
private val AddPageBg   = Color(0xFFF2F4F7)
private val AddCardBg   = Color(0xFFFFFFFF)
private val VividBlue   = Color(0xFF2563EB)
private val FieldBg     = Color(0xFFF1F3F6)
private val LabelColor  = Color(0xFF374151)
private val HintColor   = Color(0xFF9CA3AF)
private val ErrorRed    = Color(0xFFDC2626)

// Helper: parse "dd/MM/yyyy" → epoch millis (UTC)
private fun parseDateToMillis(dateStr: String): Long? = try {
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .also { it.timeZone = TimeZone.getTimeZone("UTC") }
        .parse(dateStr)?.time
} catch (_: Exception) { null }

// Helper: millis → human-readable "dd/MM/yyyy"
private fun millisToDate(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .also { it.timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(millis))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectScreen(
    editProject: ProjectEntity? = null,
    onNavigateToConfirm: (ProjectEntity) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val isEditMode = editProject != null

    // ── Form state ────────────────────────────────────────────────────────────
    var projectCode by rememberSaveable { mutableStateOf<String>(editProject?.projectCode ?: "") }
    var projectName by rememberSaveable { mutableStateOf<String>(editProject?.projectName ?: "") }
    var description by rememberSaveable { mutableStateOf<String>(editProject?.description ?: "") }
    var manager     by rememberSaveable { mutableStateOf<String>(editProject?.manager ?: "") }
    var budgetStr   by rememberSaveable { mutableStateOf(if (editProject != null) editProject.budget.toString() else "") }
    var clientInfo  by rememberSaveable { mutableStateOf<String>(editProject?.clientInfo ?: "") }
    var specialRequirements by rememberSaveable { mutableStateOf<String>(editProject?.specialRequirements ?: "") }

    val statusOptions    = listOf("Active", "On Hold", "Completed", "Cancelled")
    var selectedStatus   by rememberSaveable { mutableStateOf<String>(editProject?.status ?: statusOptions[0]) }
    var isStatusExpanded by rememberSaveable { mutableStateOf(false) }

    val priorityOptions  = listOf("Low", "Normal", "High")
    var selectedPriority by rememberSaveable { mutableStateOf<String>(editProject?.priority ?: "Normal") }

    var startDateStr by rememberSaveable { mutableStateOf<String>(editProject?.startDate ?: "") }
    var endDateStr   by rememberSaveable { mutableStateOf<String>(editProject?.endDate ?: "") }
    var showStartDatePicker by rememberSaveable { mutableStateOf(false) }
    var showEndDatePicker   by rememberSaveable { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = parseDateToMillis(editProject?.startDate ?: "")
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = parseDateToMillis(editProject?.endDate ?: "")
    )

    var showOptional by rememberSaveable {
        mutableStateOf(
            editProject?.specialRequirements?.isNotBlank() == true ||
            editProject?.clientInfo?.isNotBlank() == true
        )
    }

    // ── Validation errors ─────────────────────────────────────────────────────
    var nameError      by remember { mutableStateOf<String?>(null) }
    var descError      by remember { mutableStateOf<String?>(null) }
    var managerError   by remember { mutableStateOf<String?>(null) }
    var budgetError    by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError   by remember { mutableStateOf<String?>(null) }

    // ── Date picker dialogs ───────────────────────────────────────────────────
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let {
                        startDateStr = millisToDate(it)
                        startDateError = null
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
                    endDatePickerState.selectedDateMillis?.let {
                        endDateStr = millisToDate(it)
                        endDateError = null
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = endDatePickerState) }
    }

    // ── Validate & build ──────────────────────────────────────────────────────
    fun validate(): Boolean {
        nameError      = if (projectName.isBlank()) "Project name is required" else null
        descError      = if (description.isBlank()) "Description is required"  else null
        managerError   = if (manager.isBlank())     "Manager name is required" else null
        startDateError = if (startDateStr.isBlank()) "Select a start date" else null
        budgetError    = when {
            budgetStr.isBlank()               -> "Budget is required"
            budgetStr.toDoubleOrNull() == null -> "Enter a valid number"
            budgetStr.toDouble() < 0           -> "Budget cannot be negative"
            else                               -> null
        }
        endDateError = when {
            endDateStr.isBlank() -> "Select an end date"
            startDateStr.isNotBlank() &&
                parseDateToMillis(endDateStr) != null &&
                parseDateToMillis(startDateStr) != null &&
                parseDateToMillis(endDateStr)!! < parseDateToMillis(startDateStr)!! ->
                "End date cannot be before start date"
            else -> null
        }
        return listOf(nameError, descError, managerError, startDateError, endDateError, budgetError).all { it == null }
    }

    fun buildProject(): ProjectEntity {
        // Auto-generate a project code if not in edit mode
        val code = if (isEditMode) editProject!!.projectCode
                   else "PRJ-${System.currentTimeMillis() % 100000}"
        return ProjectEntity(
            id          = editProject?.id ?: 0,
            projectCode = code,
            projectName = projectName.trim(),
            description = description.trim(),
            startDate   = startDateStr,
            endDate     = endDateStr,
            manager     = manager.trim(),
            status      = selectedStatus,
            budget      = budgetStr.toDoubleOrNull() ?: 0.0,
            priority    = selectedPriority,
            specialRequirements = specialRequirements.trim().ifBlank { null },
            clientInfo  = clientInfo.trim().ifBlank { null }
        )
    }

    fun doReset() {
        projectName = ""; description = ""; manager = ""
        budgetStr   = ""; clientInfo  = ""; specialRequirements = ""
        selectedStatus = statusOptions[0]; selectedPriority = "Normal"
        startDateStr = ""; endDateStr = ""
        nameError = null; descError = null; managerError = null
        budgetError = null; startDateError = null; endDateError = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = AddPageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Project" else "New Project",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!isEditMode) {
                        TextButton(onClick = ::doReset) {
                            Text(
                                "Reset",
                                color = VividBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AddPageBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── BASIC INFORMATION ─────────────────────────────────────────────
            FormSectionLabel("Basic Information")
            FormCard {
                // Project Name *
                AddFormField(
                    value = projectName,
                    onValueChange = {
                        if (it.length <= 80) { projectName = it; if (it.isNotBlank()) nameError = null }
                    },
                    label = "Project Name *",
                    placeholder = "e.g. Q4 Marketing Campaign",
                    isError = nameError != null,
                    errorMessage = nameError,
                    trailingText = "${projectName.length}/80",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                // Description *
                AddFormField(
                    value = description,
                    onValueChange = {
                        if (it.length <= 300) { description = it; if (it.isNotBlank()) descError = null }
                    },
                    label = "Project Description *",
                    placeholder = "Describe project goals and scope...",
                    isError = descError != null,
                    errorMessage = descError,
                    trailingText = "${description.length}/300",
                    minLines = 2,
                    maxLines = 3,
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }

            // ── SCHEDULE ──────────────────────────────────────────────────────
            FormSectionLabel("Schedule")
            FormCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Date
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Start Date *",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (startDateError != null) ErrorRed else LabelColor
                        )
                        FormDateButton(
                            label = startDateStr.ifBlank { "dd/mm/yyyy" },
                            isPlaceholder = startDateStr.isBlank(),
                            isError = startDateError != null,
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (startDateError != null) {
                            Text(
                                startDateError!!,
                                color = ErrorRed,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                    // End Date
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "End Date *",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (endDateError != null) ErrorRed else LabelColor
                        )
                        FormDateButton(
                            label = endDateStr.ifBlank { "dd/mm/yyyy" },
                            isPlaceholder = endDateStr.isBlank(),
                            isError = endDateError != null,
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (endDateError != null) {
                            Text(
                                endDateError!!,
                                color = ErrorRed,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            // ── PROJECT MANAGER ────────────────────────────────────────────────
            FormSectionLabel("Project Details")
            FormCard {
                AddFormField(
                    value = manager,
                    onValueChange = { manager = it; if (it.isNotBlank()) managerError = null },
                    label = "Project Manager *",
                    placeholder = "Full name",
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = HintColor, modifier = Modifier.size(18.dp)) },
                    isError = managerError != null,
                    errorMessage = managerError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
            }

            // ── STATUS ────────────────────────────────────────────────────────
            FormCard {
                Text(
                    "Status *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LabelColor
                )
                ExposedDropdownMenuBox(
                    expanded = isStatusExpanded,
                    onExpandedChange = { isStatusExpanded = it }
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                        color = FieldBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Circle,
                                    null,
                                    tint = addStatusColor(selectedStatus),
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    selectedStatus,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LabelColor
                                )
                            }
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                null,
                                tint = HintColor
                            )
                        }
                    }
                    ExposedDropdownMenu(
                        expanded = isStatusExpanded,
                        onDismissRequest = { isStatusExpanded = false }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                leadingIcon = {
                                    Icon(Icons.Default.Circle, null,
                                        Modifier.size(10.dp),
                                        tint = addStatusColor(option))
                                },
                                onClick = { selectedStatus = option; isStatusExpanded = false }
                            )
                        }
                    }
                }
            }

            // ── BUDGET ────────────────────────────────────────────────────────
            FormCard {
                AddFormField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it; budgetError = null },
                    label = "Budget *",
                    placeholder = "0.00",
                    isError = budgetError != null,
                    errorMessage = budgetError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )
            }

            // ── PRIORITY ──────────────────────────────────────────────────────
            FormCard {
                Text(
                    "Priority Level *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LabelColor
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorityOptions.forEach { option ->
                        val selected = selectedPriority == option
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedPriority = option },
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

            // ── ADDITIONAL INFO (collapsible) ─────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AddCardBg),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showOptional = !showOptional },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Client / Department Information",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = LabelColor
                        )
                        Icon(
                            if (showOptional) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = HintColor
                        )
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
                                value = clientInfo,
                                onValueChange = { clientInfo = it },
                                label = "Client / Department",
                                placeholder = "e.g. Marketing Dept / Acme Corp",
                                leadingIcon = {
                                    Icon(Icons.Default.Business, null, tint = HintColor, modifier = Modifier.size(18.dp))
                                }
                            )
                            AddFormField(
                                value = specialRequirements,
                                onValueChange = { specialRequirements = it },
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

            // ── ACTIONS ───────────────────────────────────────────────────────
            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    if (validate()) {
                        onNavigateToConfirm(buildProject())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VividBlue)
            ) {
                Icon(
                    if (isEditMode) Icons.Default.Save else Icons.Default.Save,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEditMode) "Review Changes" else "Save Project",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable: white card wrapper
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FormCard(content: @Composable ColumnScope.() -> Unit) {
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
private fun AddFormField(
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
            label,
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
                    errorMessage ?: "",
                    color = ErrorRed,
                    style = MaterialTheme.typography.labelSmall
                )
                if (trailingText != null) {
                    Text(
                        trailingText,
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
private fun FormSectionLabel(title: String) {
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
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = LabelColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date picker button — matches AdvancedSearchScreen style
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FormDateButton(
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
// Status dot colour
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun addStatusColor(status: String): Color = when (status) {
    "Active"    -> Color(0xFF16A34A)
    "Completed" -> VividBlue
    "On Hold"   -> Color(0xFFF59E0B)
    else        -> Color(0xFFDC2626)
}
