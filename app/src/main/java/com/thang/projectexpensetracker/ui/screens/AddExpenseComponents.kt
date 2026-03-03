package com.thang.projectexpensetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thang.projectexpensetracker.model.ExpenseFormState
import com.thang.projectexpensetracker.ui.components.*
import com.thang.projectexpensetracker.ui.theme.*

// Screen-scoped option lists — no UI logic, pure data
internal val expenseTypes    = listOf("Travel", "Labour", "Equipment", "Food & Entertainment", "Accommodation", "Materials", "Software", "Hardware", "Office Supplies", "Other")
internal val paymentMethods  = listOf("Cash", "Credit Card", "Bank Transfer", "Cheque")
internal val paymentStatuses = listOf("Paid", "Pending", "Reimbursed")
internal val currencyOptions = listOf("USD ($)", "EUR (\u20ac)", "GBP (\u00a3)", "VND (\u20ab)", "JPY (\u00a5)", "AUD ($)", "CAD ($)", "SGD ($)")

// ─────────────────────────────────────────────────────────────────────────────
// ExpenseCoreSection — date, amount+currency, expense type
// Single Responsibility: core required field rendering only.
// Dropdown UI states (expanded) are local to this section.
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpenseCoreSection(
    state: ExpenseFormState,
    onUpdate: (ExpenseFormState) -> Unit,
    onDateClick: () -> Unit
) {
    var isCurrencyExpanded by remember { mutableStateOf(false) }
    var isTypeExpanded     by remember { mutableStateOf(false) }

    // ── Date ──────────────────────────────────────────────────────────────────
    ExpFormLabel("Date of Expense", required = true, error = state.dateError)
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value         = state.dateStr,
            onValueChange = {},
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = { Text("dd/MM/yyyy", color = HintColor) },
            trailingIcon  = { Icon(Icons.Default.DateRange, null, tint = HintColor) },
            readOnly      = true,
            isError       = state.dateError != null,
            shape         = RoundedCornerShape(12.dp),
            colors        = expOutlinedFieldColors(state.dateError != null),
            singleLine    = true
        )
        Box(
            modifier = Modifier.matchParentSize().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { onDateClick() }
        )
    }
    if (state.dateError != null) ExpErrorText(state.dateError)

    // ── Amount + Currency ─────────────────────────────────────────────────────
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            ExpFormLabel("Amount", required = true, error = state.amountError)
            OutlinedTextField(
                value           = state.amountStr,
                onValueChange   = { onUpdate(state.copy(amountStr = it, amountError = null)) },
                modifier        = Modifier.fillMaxWidth(),
                placeholder     = { Text("0.00", color = HintColor) },
                singleLine      = true,
                isError         = state.amountError != null,
                shape           = RoundedCornerShape(12.dp),
                colors          = expOutlinedFieldColors(state.amountError != null),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
            if (state.amountError != null) ExpErrorText(state.amountError)
        }
        Column(modifier = Modifier.weight(1f)) {
            ExpFormLabel("Currency", required = true)
            ExposedDropdownMenuBox(expanded = isCurrencyExpanded, onExpandedChange = { isCurrencyExpanded = it }) {
                OutlinedTextField(
                    value         = state.currency,
                    onValueChange = {},
                    readOnly      = true,
                    modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCurrencyExpanded) },
                    shape         = RoundedCornerShape(12.dp),
                    colors        = expOutlinedFieldColors(false),
                    singleLine    = true
                )
                ExposedDropdownMenu(expanded = isCurrencyExpanded, onDismissRequest = { isCurrencyExpanded = false }) {
                    currencyOptions.forEach { c ->
                        DropdownMenuItem(
                            text    = { Text(c) },
                            onClick = { onUpdate(state.copy(currency = c.substringBefore(" ").trim())); isCurrencyExpanded = false }
                        )
                    }
                }
            }
        }
    }

    // ── Type of Expense ───────────────────────────────────────────────────────
    ExpFormLabel("Type of Expense", required = true, error = state.typeError)
    ExposedDropdownMenuBox(expanded = isTypeExpanded, onExpandedChange = { isTypeExpanded = it }) {
        OutlinedTextField(
            value         = state.selectedType.ifBlank { "" },
            onValueChange = {},
            readOnly      = true,
            modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            placeholder   = { Text("Select category", color = HintColor) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeExpanded) },
            isError       = state.typeError != null,
            shape         = RoundedCornerShape(12.dp),
            colors        = expOutlinedFieldColors(state.typeError != null),
            singleLine    = true
        )
        ExposedDropdownMenu(expanded = isTypeExpanded, onDismissRequest = { isTypeExpanded = false }) {
            expenseTypes.forEach { t ->
                DropdownMenuItem(
                    text    = { Text(t) },
                    onClick = { onUpdate(state.copy(selectedType = t, typeError = null)); isTypeExpanded = false }
                )
            }
        }
    }
    if (state.typeError != null) ExpErrorText(state.typeError)
}

// ─────────────────────────────────────────────────────────────────────────────
// ExpensePaymentSection — payment method, claimant, payment status
// Single Responsibility: payment & claimant field rendering only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun ExpensePaymentSection(
    state: ExpenseFormState,
    onUpdate: (ExpenseFormState) -> Unit
) {
    ExpFormLabel("Payment Method", required = true)
    ToggleButtonGrid(
        options  = paymentMethods,
        selected = state.selectedPayment,
        onSelect = { onUpdate(state.copy(selectedPayment = it)) }
    )

    ExpFormLabel("Claimant", required = true, error = state.claimantError)
    OutlinedTextField(
        value           = state.claimant,
        onValueChange   = { onUpdate(state.copy(claimant = it, claimantError = if (it.isNotBlank()) null else state.claimantError)) },
        modifier        = Modifier.fillMaxWidth(),
        placeholder     = { Text("Full name", color = HintColor) },
        leadingIcon     = { Icon(Icons.Default.Person, null, tint = HintColor) },
        isError         = state.claimantError != null,
        singleLine      = true,
        shape           = RoundedCornerShape(12.dp),
        colors          = expOutlinedFieldColors(state.claimantError != null),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
    )
    if (state.claimantError != null) ExpErrorText(state.claimantError)

    ExpFormLabel("Payment Status", required = true)
    ToggleButtonRow(
        options  = paymentStatuses,
        selected = state.selectedStatus,
        onSelect = { onUpdate(state.copy(selectedStatus = it)) }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// ExpenseOptionalSection — location (GPS) + free-text description
// Single Responsibility: optional supplementary field rendering only.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun ExpenseOptionalSection(
    state: ExpenseFormState,
    onUpdate: (ExpenseFormState) -> Unit,
    onGetLocation: () -> Unit
) {
    ExpFormLabel("Location", optional = true)
    OutlinedTextField(
        value         = state.location,
        onValueChange = { onUpdate(state.copy(location = it)) },
        modifier      = Modifier.fillMaxWidth(),
        placeholder   = { Text("City, Office, or Vendor", color = HintColor) },
        leadingIcon   = { Icon(Icons.Default.LocationOn, null, tint = HintColor) },
        trailingIcon  = {
            if (state.isLocating) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else IconButton(onClick = onGetLocation) {
                Icon(Icons.Default.MyLocation, "GPS", tint = VividBlue)
            }
        },
        singleLine    = true,
        shape         = RoundedCornerShape(12.dp),
        colors        = expOutlinedFieldColors(false)
    )

    ExpFormLabel("Description", optional = true)
    OutlinedTextField(
        value           = state.description,
        onValueChange   = { onUpdate(state.copy(description = it)) },
        modifier        = Modifier.fillMaxWidth(),
        placeholder     = { Text("Add additional notes here...", color = HintColor) },
        minLines        = 3,
        maxLines        = 5,
        shape           = RoundedCornerShape(12.dp),
        colors          = expOutlinedFieldColors(false),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
}
