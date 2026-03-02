package com.thang.projectexpensetracker.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ─── Design tokens ────────────────────────────────────────────────────────────
private val ExpBg       = Color(0xFFF2F4F7)
private val ExpCard     = Color(0xFFFFFFFF)
private val ExpBlue     = Color(0xFF2563EB)
private val ExpLabelGrey= Color(0xFF6B7280)
private val ExpDark     = Color(0xFF111827)
private val ExpFieldBg  = Color(0xFFF9FAFB)
private val ExpBorder   = Color(0xFFE5E7EB)
private val ExpRed      = Color(0xFFDC2626)

private fun parseDateToMillisExp(dateStr: String): Long? = try {
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .also { it.timeZone = TimeZone.getTimeZone("UTC") }
        .parse(dateStr)?.time
} catch (_: Exception) { null }

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    projectId: Long,
    editExpense: ExpenseEntity? = null,
    onNavigateToConfirm: (ExpenseEntity) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val context    = LocalContext.current
    val isEditMode = editExpense != null

    // ── Form state (rememberSaveable so nav back restores values) ────────────
    var dateStr     by rememberSaveable { mutableStateOf<String>(editExpense?.date     ?: "") }
    var amountStr   by rememberSaveable { mutableStateOf<String>(if (editExpense != null) editExpense.amount.toString() else "") }
    var currency    by rememberSaveable { mutableStateOf<String>(editExpense?.currency ?: "USD") }
    var claimant    by rememberSaveable { mutableStateOf<String>(editExpense?.claimant ?: "") }
    var description by rememberSaveable { mutableStateOf<String>(editExpense?.description ?: "") }
    var location    by rememberSaveable { mutableStateOf<String>(editExpense?.location  ?: "") }
    var isLocating  by rememberSaveable { mutableStateOf(false) }

    val paymentMethods   = listOf("Cash", "Credit Card", "Bank Transfer", "Cheque")
    var selectedPayment  by rememberSaveable { mutableStateOf<String>(editExpense?.paymentMethod ?: paymentMethods[0]) }

    val expenseTypes   = listOf("Travel", "Labour", "Equipment", "Food & Entertainment", "Accommodation", "Materials", "Software", "Hardware", "Office Supplies", "Other")
    var selectedType   by rememberSaveable { mutableStateOf<String>(editExpense?.type ?: "") }
    var isTypeExpanded by rememberSaveable { mutableStateOf(false) }

    val paymentStatuses   = listOf("Paid", "Pending", "Reimbursed")
    var selectedStatus    by rememberSaveable { mutableStateOf<String>(editExpense?.paymentStatus ?: "Pending") }

    val currencyOptions    = listOf("USD ($)", "EUR (€)", "GBP (£)", "VND (₫)", "JPY (¥)", "AUD ($)", "CAD ($)", "SGD ($)")
    var isCurrencyExpanded by rememberSaveable { mutableStateOf(false) }

    var showDatePicker  by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = parseDateToMillisExp(editExpense?.date ?: "")
    )

    // ── Validation errors ─────────────────────────────────────────────────────
    var dateError     by remember { mutableStateOf<String?>(null) }
    var amountError   by remember { mutableStateOf<String?>(null) }
    var claimantError by remember { mutableStateOf<String?>(null) }
    var typeError     by remember { mutableStateOf<String?>(null) }

    // ── Location  ─────────────────────────────────────────────────────────────
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            isLocating = true
            LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc -> isLocating = false; loc?.let { location = "%.5f, %.5f".format(it.latitude, it.longitude) } }
                .addOnFailureListener { isLocating = false }
        }
    }
    fun getLocation() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                isLocating = true
                LocationServices.getFusedLocationProviderClient(context)
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc -> isLocating = false; loc?.let { location = "%.5f, %.5f".format(it.latitude, it.longitude) } }
                    .addOnFailureListener { isLocating = false }
            }
            else -> locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // ── Date picker dialog ────────────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).also { it.timeZone = TimeZone.getTimeZone("UTC") }
                        dateStr   = sdf.format(Date(millis))
                        dateError = null
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    fun validate(): Boolean {
        dateError     = if (dateStr.isBlank())    "Please select a date"      else null
        claimantError = if (claimant.isBlank())   "Required"                  else null
        typeError     = if (selectedType.isBlank()) "Please select a category" else null
        amountError   = when {
            amountStr.isBlank()                -> "Required"
            amountStr.toDoubleOrNull() == null -> "Invalid number"
            amountStr.toDouble() <= 0          -> "Must be > 0"
            else                               -> null
        }
        return listOf(dateError, amountError, claimantError, typeError).all { it == null }
    }

    fun buildExpense(): ExpenseEntity = ExpenseEntity(
        expenseId     = editExpense?.expenseId ?: 0,
        projectId     = projectId,
        date          = dateStr,
        amount        = amountStr.toDoubleOrNull() ?: 0.0,
        currency      = currency.substringBefore(" ").trim(),
        type          = selectedType,
        paymentMethod = selectedPayment,
        claimant      = claimant.trim(),
        paymentStatus = selectedStatus,
        description   = description.trim().ifBlank { null },
        location      = location.trim().ifBlank { null }
    )

    // ── Auto-generate expense code display ────────────────────────────────────
    val expenseCode = if (isEditMode)
        "EXP-${editExpense!!.expenseId}"
    else
        "EXP-${System.currentTimeMillis() % 100000}"

    Scaffold(
        containerColor = ExpBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Expense" else "Add New Expense",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ExpBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Expense ID (locked) ───────────────────────────────────────────
            ExpFormLabel("Expense ID")
            ExpLockedField(value = expenseCode)

            // ── Date of Expense ───────────────────────────────────────────────
            ExpFormLabel("Date of Expense", required = true, error = dateError)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value          = dateStr,
                    onValueChange  = {},
                    modifier       = Modifier.fillMaxWidth(),
                    placeholder    = { Text("mm/dd/yyyy", color = ExpLabelGrey) },
                    trailingIcon   = { Icon(Icons.Default.DateRange, null, tint = ExpLabelGrey) },
                    readOnly       = true,
                    isError        = dateError != null,
                    shape          = RoundedCornerShape(12.dp),
                    colors         = expFieldColors(dateError != null),
                    singleLine     = true
                )
                Box(
                    modifier = Modifier.matchParentSize().clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { showDatePicker = true }
                )
            }
            if (dateError != null) ExpErrorText(dateError!!)

            // ── Amount + Currency ─────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    ExpFormLabel("Amount", required = true, error = amountError)
                    OutlinedTextField(
                        value         = amountStr,
                        onValueChange = { amountStr = it; amountError = null },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = { Text("0.00", color = ExpLabelGrey) },
                        singleLine    = true,
                        isError       = amountError != null,
                        shape         = RoundedCornerShape(12.dp),
                        colors        = expFieldColors(amountError != null),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                    if (amountError != null) ExpErrorText(amountError!!)
                }
                Column(modifier = Modifier.weight(1f)) {
                    ExpFormLabel("Currency", required = true)
                    ExposedDropdownMenuBox(
                        expanded       = isCurrencyExpanded,
                        onExpandedChange = { isCurrencyExpanded = it }
                    ) {
                        OutlinedTextField(
                            value         = currency,
                            onValueChange = {},
                            readOnly      = true,
                            modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCurrencyExpanded) },
                            shape         = RoundedCornerShape(12.dp),
                            colors        = expFieldColors(false),
                            singleLine    = true
                        )
                        ExposedDropdownMenu(expanded = isCurrencyExpanded, onDismissRequest = { isCurrencyExpanded = false }) {
                            currencyOptions.forEach { c ->
                                DropdownMenuItem(
                                    text    = { Text(c) },
                                    onClick = { currency = c.substringBefore(" ").trim(); isCurrencyExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // ── Type of Expense ───────────────────────────────────────────────
            ExpFormLabel("Type of Expense", required = true, error = typeError)
            ExposedDropdownMenuBox(expanded = isTypeExpanded, onExpandedChange = { isTypeExpanded = it }) {
                OutlinedTextField(
                    value         = selectedType.ifBlank { "" },
                    onValueChange = {},
                    readOnly      = true,
                    modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    placeholder   = { Text("Select category", color = ExpLabelGrey) },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeExpanded) },
                    isError       = typeError != null,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = expFieldColors(typeError != null),
                    singleLine    = true
                )
                ExposedDropdownMenu(expanded = isTypeExpanded, onDismissRequest = { isTypeExpanded = false }) {
                    expenseTypes.forEach { t ->
                        DropdownMenuItem(
                            text    = { Text(t) },
                            onClick = { selectedType = t; typeError = null; isTypeExpanded = false }
                        )
                    }
                }
            }
            if (typeError != null) ExpErrorText(typeError!!)

            // ── Payment Method — toggle buttons ────────────────────────────────
            ExpFormLabel("Payment Method", required = true)
            ToggleButtonGrid(
                options   = paymentMethods,
                selected  = selectedPayment,
                onSelect  = { selectedPayment = it },
                columns   = 2
            )

            // ── Claimant ──────────────────────────────────────────────────────
            ExpFormLabel("Claimant", required = true, error = claimantError)
            OutlinedTextField(
                value         = claimant,
                onValueChange = { claimant = it; if (it.isNotBlank()) claimantError = null },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("Full name", color = ExpLabelGrey) },
                leadingIcon   = { Icon(Icons.Default.Person, null, tint = ExpLabelGrey) },
                isError       = claimantError != null,
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = expFieldColors(claimantError != null),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
            )
            if (claimantError != null) ExpErrorText(claimantError!!)

            // ── Payment Status — toggle buttons ────────────────────────────────
            ExpFormLabel("Payment Status", required = true)
            ToggleButtonRow(
                options  = paymentStatuses,
                selected = selectedStatus,
                onSelect = { selectedStatus = it }
            )

            // ── Location (Optional) ───────────────────────────────────────────
            ExpFormLabel("Location", optional = true)
            OutlinedTextField(
                value         = location,
                onValueChange = { location = it },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("City, Office, or Vendor", color = ExpLabelGrey) },
                leadingIcon   = { Icon(Icons.Default.LocationOn, null, tint = ExpLabelGrey) },
                trailingIcon  = {
                    if (isLocating) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else IconButton(onClick = { getLocation() }) {
                        Icon(Icons.Default.MyLocation, "GPS", tint = ExpBlue)
                    }
                },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = expFieldColors(false)
            )

            // ── Description (Optional) ────────────────────────────────────────
            ExpFormLabel("Description", optional = true)
            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("Add additional notes here...", color = ExpLabelGrey) },
                minLines      = 3,
                maxLines      = 5,
                shape         = RoundedCornerShape(12.dp),
                colors        = expFieldColors(false),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            // ── Confirm Button ────────────────────────────────────────────────
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    if (validate()) {
                        val expense = buildExpense()
                        onNavigateToConfirm(expense)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = ExpBlue)
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEditMode) "Review Changes" else "Confirm Expense Details",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    fontSize   = 16.sp
                )
            }

            TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = ExpLabelGrey, style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2×N Toggle button grid (for Payment Method)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ToggleButtonGrid(
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

// ─────────────────────────────────────────────────────────────────────────────
// Single-row toggle buttons (for Payment Status — 3 options)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ToggleButtonRow(
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
// Individual toggle button
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ToggleButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isActive) ExpBlue else ExpBorder
    val bgColor     = if (isActive) Color(0xFFEFF6FF) else ExpCard
    val textColor   = if (isActive) ExpBlue else ExpDark

    Box(
        modifier          = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment  = Alignment.Center
    ) {
        Text(
            label,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color      = textColor,
            textAlign  = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Field label with optional * and (Optional) suffix
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExpFormLabel(label: String, required: Boolean = false, optional: Boolean = false, error: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = if (error != null) ExpRed else ExpDark
        )
        if (required) {
            Text("*", color = ExpRed, fontWeight = FontWeight.Bold)
        }
        if (optional) {
            Text("(Optional)", style = MaterialTheme.typography.bodySmall, color = ExpLabelGrey)
        }
    }
}

@Composable
private fun ExpErrorText(msg: String) {
    Text(msg, style = MaterialTheme.typography.labelSmall, color = ExpRed)
}

// ─────────────────────────────────────────────────────────────────────────────
// Locked/auto-generated ID field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExpLockedField(value: String) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ExpFieldBg)
            .border(1.dp, ExpBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(value, style = MaterialTheme.typography.bodyMedium, color = ExpLabelGrey)
        Icon(Icons.Default.Lock, null, tint = ExpLabelGrey, modifier = Modifier.size(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OutlinedTextField colour scheme matching the design image
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun expFieldColors(isError: Boolean) = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor  = if (isError) ExpRed else ExpBorder,
    focusedBorderColor    = if (isError) ExpRed else ExpBlue,
    unfocusedContainerColor = ExpFieldBg,
    focusedContainerColor   = ExpCard,
    errorBorderColor        = ExpRed
)
