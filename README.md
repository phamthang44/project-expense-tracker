# 📊 Project Expense Tracker

A modern **Android** application for managing project budgets and tracking expenses, built with **Kotlin** and **Jetpack Compose**. Data is persisted locally with **Room (SQLite)** and can be synchronised to the cloud via **Firebase Cloud Firestore**.

> **Course:** GCS230575 / COMP1786 – Mobile Development  
> **Author:** Pham Thang

---

## ✨ Features

| Category | Details |
|---|---|
| **Project Management** | Create, read, update and delete projects with code, name, description, dates, manager, status, budget, priority and more |
| **Expense Tracking** | Record expenses against a project – amount, currency, type, payment method, claimant, payment status, location (GPS) |
| **Cloud Sync** | One-tap upload of all data to Firebase Firestore; auto-sync option available |
| **Advanced Search** | Search projects by keyword, status, owner, and date range |
| **Dark / Light / System Theme** | User-selectable theme persisted across app restarts |
| **Confirmation Flow** | Review-before-save screens for both projects and expenses |
| **Offline Support** | Full CRUD works offline; sync when ready |

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI Framework | Jetpack Compose (Material 3) |
| Navigation | Navigation Compose 2.9.7 |
| Local Database | Room 2.8.4 (SQLite) |
| Cloud Backend | Firebase Cloud Firestore (BOM 33.7.0) |
| Architecture | MVVM (ViewModel + StateFlow) |
| Location | Google Play Services Location 21.3.0 |
| Build System | Gradle (Kotlin DSL) with Version Catalog |
| Min SDK | 24 (Android 7.0) · Target SDK 36 |

---

## 📂 Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/thang/projectexpensetracker/
│   ├── MainActivity.kt               # Single-activity entry, NavHost
│   ├── data/
│   │   ├── AppDatabase.kt            # Room database singleton
│   │   ├── dao/
│   │   │   ├── ProjectDao.kt
│   │   │   └── ExpenseDao.kt
│   │   └── entity/
│   │       ├── ProjectEntity.kt
│   │       └── ExpenseEntity.kt
│   ├── infrastructure/
│   │   ├── FirebaseHelper.kt          # Firestore CRUD operations
│   │   ├── ProjectRepository.kt       # Bridge between Room & Firestore
│   │   └── ThemePreferences.kt        # SharedPreferences for theme
│   ├── model/
│   │   ├── ExpenseFormState.kt
│   │   ├── ProjectFormState.kt
│   │   └── SyncStatus.kt
│   ├── ui/
│   │   ├── components/
│   │   │   └── SharedComponents.kt    # Reusable UI components
│   │   ├── screens/
│   │   │   ├── AdminDashboardScreen.kt
│   │   │   ├── AddProjectScreen.kt
│   │   │   ├── AddExpenseScreen.kt
│   │   │   ├── ProjectDetailScreen.kt
│   │   │   ├── ExpenseListScreen.kt
│   │   │   ├── ConfirmationScreen.kt
│   │   │   ├── ExpenseConfirmationScreen.kt
│   │   │   ├── SyncScreen.kt
│   │   │   ├── AdvancedSearchScreen.kt
│   │   │   ├── SettingsScreen.kt
│   │   │   └── ... (+ Component files)
│   │   └── theme/
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   ├── util/
│   │   ├── DateUtils.kt
│   │   ├── FormatUtils.kt
│   │   └── NetworkUtils.kt
│   └── viewmodel/
│       ├── ProjectViewModel.kt
│       ├── AddProjectViewModel.kt
│       ├── ExpenseViewModel.kt
│       └── AddExpenseViewModel.kt
└── res/                               # Drawables, strings, themes …
```

---

## 🚀 Prerequisites

| Requirement | Version |
|---|---|
| **Android Studio** | Ladybug (2024.2+) or later |
| **JDK** | 11 or higher |
| **Android SDK** | API 36 (compile), API 24+ (minimum) |
| **Gradle** | Ships with the project wrapper (`gradlew`) |
| **Firebase Account** | Free Spark plan is sufficient |

---

## 📥 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/phamthang44/project-expense-tracker.git
cd project-expense-tracker
```

### 2. Open in Android Studio

1. Launch **Android Studio**.
2. Select **File → Open** and navigate to the cloned project folder.
3. Wait for Gradle sync to complete (this may take a few minutes on first run).

### 3. Set Up Firebase (Required for Cloud Sync)

The `google-services.json` file is **not** included in the repository for security reasons. You must create your own Firebase project:

#### 3a. Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** and follow the wizard:
   - Enter a project name (e.g. `ProjectExpenseTracker`).
   - Enable or disable Google Analytics (optional).
   - Click **Create project**.

#### 3b. Register Your Android App

1. In your Firebase project, click the **Android** icon (🤖) to add an Android app.
2. Enter the **package name**: `com.thang.projectexpensetracker`
3. (Optional) Enter a nickname and SHA-1 signing certificate.
4. Click **Register app**.

#### 3c. Download `google-services.json`

1. Click **Download google-services.json**.
2. Place the downloaded file in:

   ```
   ProjectExpenseTracker/app/google-services.json
   ```

   > ⚠️ The file must be in the **`app/`** directory (not the project root).

#### 3d. Enable Cloud Firestore

1. In the Firebase Console, go to **Build → Firestore Database**.
2. Click **Create database**.
3. Choose a location (select the region closest to you).
4. Start in **test mode** (for development):

   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if true;
       }
     }
   }
   ```

   > ⚠️ **Test-mode rules allow anyone to read/write.** For production, configure proper security rules.

#### 3e. Firestore Collections (Auto-Created)

The app automatically creates two collections when data is synced:

| Collection | Document ID | Description |
|---|---|---|
| `projects` | `{projectCode}` | Each project is stored with its `projectCode` as the document ID |
| `expenses` | `{projectId}_{expenseId}` | Each expense uses a composite key as document ID |

No manual collection setup is required — just enable Firestore and the app handles the rest.

### 4. Build & Run

#### Option A: From Android Studio

1. Connect a physical Android device (USB debugging enabled) or start an **Android Emulator** (API 24+).
2. Click the **Run ▶️** button or press `Shift + F10`.

#### Option B: From the Command Line

```bash
# Windows
.\gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

The APK will be generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install on a connected device:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 App Permissions

| Permission | Purpose |
|---|---|
| `ACCESS_FINE_LOCATION` | Capture GPS coordinates when recording an expense |
| `ACCESS_COARSE_LOCATION` | Fallback location provider |
| `INTERNET` | Cloud sync with Firebase Firestore |
| `ACCESS_NETWORK_STATE` | Detect online/offline status for sync |

---

## 🧭 App Navigation

```
Admin Dashboard (Home)
├── Add Project → Confirmation → Save
├── Project Detail
│   ├── Edit Project → Confirmation → Update
│   ├── Add Expense → Confirmation → Save
│   ├── View All Expenses
│   │   ├── Edit Expense → Confirmation → Update
│   │   └── Delete Expense
│   └── Delete Project
├── Advanced Search → Project Detail
├── Cloud Sync (Sync Now / Auto-Sync)
└── Settings (Theme: Light / Dark / System)
```

---

## 🛠️ Troubleshooting

| Issue | Solution |
|---|---|
| **Gradle sync fails** | Check your internet connection; ensure Android SDK 36 is installed via SDK Manager |
| **`google-services.json` not found** | Make sure the file is in `app/` directory, not the project root |
| **Firebase sync fails** | Verify Firestore is enabled in the Firebase Console and security rules allow writes |
| **Location not working** | Grant location permissions when the app requests them; ensure GPS/Location Services is enabled on the device |
| **Build error with KSP** | Run **File → Invalidate Caches / Restart** in Android Studio, then rebuild |

---

## 📄 License

This project is developed as part of university coursework (COMP1786). All rights reserved.
