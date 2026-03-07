You are an experienced mobile software engineer and university coursework reviewer.

I need help generating a **10-minute demonstration script** for my university coursework project.

The script will be used to record a **screen recording video explaining my app implementation and source code**.

The goal of the video is to clearly demonstrate:

1. The application features
2. The user interface
3. The source code that implements each feature

The script must follow this pattern for every feature:

UI → Feature explanation → Show code → Explain code → Show result

The explanation should be simple and suitable for a student presenting their work.

---

# Project Context

This project is a **Project Expense Tracker App**.

The system contains two apps:

1. **Admin App (Android Native)**
2. **User App (Hybrid Mobile App)**

---

# Technologies Used

Admin App:
- Language: **Kotlin**
- UI Framework: **Jetpack Compose**
- Design System: **Material 3**
- Local Database: **SQLite / Room**
- Architecture: MVVM
- Android Studio project

Important note:
Jetpack Compose uses **Composable functions instead of XML layouts**, so there is **no generated layout code**.

User App:
- Framework: **React Native**
- Language: **JavaScript / TypeScript**
- UI implemented with **React components**

---

# Coursework Features That Must Be Demonstrated

Feature A – Create Project  
Feature B – View / Edit / Delete Projects  
Feature C – Manage Expenses  
Feature D – Search Projects  
Feature E – Upload data to cloud API  
Feature G – Hybrid app showing projects from cloud  
Feature H – Favourite projects in hybrid app

---

# What I Need You To Generate

Generate a **complete 10-minute video demonstration script**.

For each feature include:

1. What the presenter should show on the screen
2. What the presenter should say
3. Which source code file to open
4. What part of the code to explain

---

# Important Instructions

Because this project uses **Jetpack Compose**, code explanation should focus on:

- Composable UI functions
- Material 3 components
- ViewModel logic
- State management
- Database interaction

Avoid mentioning XML layouts because this project does not use them.

For the React Native app, explain:

- React components
- API calls
- State management
- UI screens

---

# Output Format

Structure the result like this:

INTRODUCTION

Feature A – Create Project
- What to show on screen
- What to say
- Code file to open
- Code explanation

Feature B – View Projects
...

Feature C – Expense Management
...

Feature D – Search
...

Feature E – Cloud Upload
...

Feature G – React Native Hybrid App
...

Feature H – Favourite Projects
...

CONCLUSION

---

# Tone Requirements

The explanation must:

- sound natural for a student speaking
- be easy to read aloud
- avoid complex technical jargon
- be about 9–10 minutes long

---

# Additional Task

Also generate a **short checklist of code files I should show during the demo**, for example:

Admin App:
- ProjectViewModel.kt
- ProjectRepository.kt
- AddProjectScreen.kt
- ExpenseScreen.kt

Hybrid App:
- ProjectListScreen.js
- ApiService.js
- FavouriteScreen.js

---

Now wait for me to provide my project structure and code files before generating the final script.