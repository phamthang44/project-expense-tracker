# AddProjectScreen Refactoring Plan

## 1. State Management & ViewModel (Step 1)
- **Create `ProjectFormState`**: Define a data class `ProjectFormState` in `ProjectViewModel.kt` containing all form fields (`projectName`, `description`, etc.) and their respective error messages (`nameError`, `descError`, etc.).
- **Update `ProjectViewModel`**: 
  - Add `val projectFormState = MutableStateFlow(ProjectFormState())`
  - Implement `updateFormState(newState: ProjectFormState)`
  - Migrate `validate()`, `buildProject(isEditMode: Boolean, existingId: Long, existingCode: String)`, and `doReset()` from `AddProjectScreen.kt` into `ProjectViewModel`.

## 2. Reusable UI Components (Step 2)
- **Extract to `SharedComponents.kt`**:
  - Move `FormCard` snippet to `SharedComponents.kt`
  - Move `AddFormField` snippet to `SharedComponents.kt`
  - Move `FormSectionLabel` snippet to `SharedComponents.kt`
  - Move `FormDateButton` snippet to `SharedComponents.kt`
- **Break down `AddProjectScreen`**:
  - Create `@Composable fun BasicInfoSection(state, onStateChange)`
  - Create `@Composable fun ScheduleSection(state, onStateChange, onShowStartDatePicker, onShowEndDatePicker)`
  - Create `@Composable fun ProjectDetailsSection(state, onStateChange)`
  - Create `@Composable fun AdditionalInfoSection(state, onStateChange)`
  - Reassemble these sections cleanly inside the main `AddProjectScreen`.

## 3. Theming & Colors (Step 3)
- **Move constants to `Color.kt`**:
  - Extract `AddPageBg`, `AddCardBg`, `VividBlue`, `FieldBg`, `LabelColor`, `HintColor`, `ErrorRed` from `AddProjectScreen.kt` to `com.thang.projectexpensetracker.ui.theme.Color.kt`.
- **Apply to `AddProjectScreen` & Components**: Ensure `SharedComponents` and the updated `AddProjectScreen` refer to `Color.kt` constants directly, removing local hardcoded values.

---

## Verification Plan
### Build Check
- Run `./gradlew assembleDebug` to ensure there are no compilation errors after code motion.
### Manual UI Check
- Launch the App, go to Admin Dashboard, click "+".
- Verify that `AddProjectScreen` looks identical to before.
- Validate that the reset button and forms act normally (which validates ViewModel state handling).
- Validate that attempting to save an empty project triggers the error messages properly without crashing.
