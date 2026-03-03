package com.thang.projectexpensetracker.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import com.thang.projectexpensetracker.model.ProjectFormState
import com.thang.projectexpensetracker.ui.theme.*
import com.thang.projectexpensetracker.viewmodel.AddProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectScreen(
    viewModel: AddProjectViewModel,
    editProject: ProjectEntity? = null,
    onNavigateToConfirm: (ProjectEntity) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val isEditMode = editProject != null

    LaunchedEffect(editProject) {
        if (editProject != null) {
            // Edit mode: pre-fill form from the existing project entity.
            viewModel.initProjectFormForEdit(editProject)
        }
        // Add mode (editProject == null): the form was already reset by the caller
        // (MainActivity) before navigating here. When the user returns from
        // ConfirmationScreen via back / Edit Entry, this effect does NOT re-run
        // for the same null key, so the ViewModel form state is preserved intact.
    }

    val state by viewModel.projectFormState.collectAsState()

    var showOptional by remember {
        mutableStateOf(
            editProject?.specialRequirements?.isNotBlank() == true ||
            editProject?.clientInfo?.isNotBlank() == true
        )
    }

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
                        TextButton(onClick = { viewModel.resetProjectForm() }) {
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
            val onUpdate: (ProjectFormState) -> Unit = { viewModel.updateProjectFormState { _ -> it } }
            BasicInfoSection(state, onUpdate)
            ScheduleSection(state, onUpdate)
            ProjectDetailsSection(state, onUpdate)
            AdditionalInfoSection(state, onUpdate, showOptional, { showOptional = it })

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    if (viewModel.validateProjectForm()) {
                        onNavigateToConfirm(viewModel.buildProjectFromForm(editProject))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VividBlue)
            ) {
                Icon(
                    Icons.Default.Save,
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
