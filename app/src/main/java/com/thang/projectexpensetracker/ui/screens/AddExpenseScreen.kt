package com.thang.projectexpensetracker.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.model.ExpenseFormState
import com.thang.projectexpensetracker.ui.components.*
import com.thang.projectexpensetracker.ui.theme.*
import com.thang.projectexpensetracker.util.DateUtils
import com.thang.projectexpensetracker.viewmodel.AddExpenseViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: AddExpenseViewModel,
    projectId: Long,
    editExpense: ExpenseEntity? = null,
    onNavigateToConfirm: (ExpenseEntity) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val context    = LocalContext.current
    val isEditMode = editExpense != null

    // ── Initialise form for edit mode; add mode is reset by caller ─────────
    LaunchedEffect(editExpense) {
        if (editExpense != null) {
            viewModel.initExpenseFormForEdit(editExpense)
        }
    }

    val state by viewModel.expenseFormState.collectAsState()
    val onUpdate: (ExpenseFormState) -> Unit = { viewModel.updateExpenseFormState { _ -> it } }

    // ── UI-only: date picker ──────────────────────────────────────────────
    var showDatePicker  by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.parseDateToMillis(editExpense?.date ?: "")
    )

    // ── Location permission + GPS fetch ───────────────────────────────────
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.updateExpenseFormState { s -> s.copy(isLocating = true) }
            LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    viewModel.updateExpenseFormState { s ->
                        s.copy(
                            isLocating = false,
                            location   = if (loc != null) "%.5f, %.5f".format(loc.latitude, loc.longitude) else s.location
                        )
                    }
                }
                .addOnFailureListener {
                    viewModel.updateExpenseFormState { s -> s.copy(isLocating = false) }
                }
        }
    }
    fun getLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.updateExpenseFormState { s -> s.copy(isLocating = true) }
            LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    viewModel.updateExpenseFormState { s ->
                        s.copy(
                            isLocating = false,
                            location   = if (loc != null) "%.5f, %.5f".format(loc.latitude, loc.longitude) else s.location
                        )
                    }
                }
                .addOnFailureListener {
                    viewModel.updateExpenseFormState { s -> s.copy(isLocating = false) }
                }
        } else {
            locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // ── Date picker dialog ────────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.updateExpenseFormState { s ->
                            s.copy(dateStr = DateUtils.millisToDisplayDate(millis), dateError = null)
                        }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    val expenseCode = editExpense?.let { "EXP-${it.expenseId}" }
                     ?: "EXP-${System.currentTimeMillis() % 100000}"

    Scaffold(
        containerColor = AddPageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Expense" else "Add New Expense",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AddPageBg)
            )
        }
    ) { padding ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            ExpFormLabel("Expense ID")
            ExpLockedField(value = expenseCode)

            ExpenseCoreSection(state, onUpdate, onDateClick = { showDatePicker = true })
            ExpensePaymentSection(state, onUpdate)
            ExpenseOptionalSection(state, onUpdate, onGetLocation = { getLocation() })

            Spacer(Modifier.height(4.dp))

            Button(
                onClick  = {
                    if (viewModel.validateExpenseForm()) {
                        onNavigateToConfirm(viewModel.buildExpenseFromForm(editExpense, projectId))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = VividBlue)
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
                Text("Cancel", color = HintColor, style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
