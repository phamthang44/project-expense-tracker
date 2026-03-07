# COMP1786 Coursework – Project Expense Tracker App

## Overview
Build a **Project Expense Tracker system** consisting of:

1. **Admin App (Native Android - Kotlin)**
2. **User App (Hybrid - React Native)**

Architecture:

Mobile Admin App
→ Local SQLite Database
→ Cloud Web Service
→ Hybrid User App (connects to cloud)

The Admin app manages projects and expenses.  
The User app views projects and interacts with the cloud data.

---

# PART A — IMPLEMENTATION (80%)

## I. Admin App (Android Native - Java)

### Feature A — Create Project (10%)

Users can create a **Project**.

Required fields:
- Project ID / Code
- Project Name
- Project Description
- Start Date
- End Date
- Project Manager / Owner
- Project Status
  - Active
  - Completed
  - On Hold
- Project Budget

Optional fields:
- Special Requirements
- Client / Department Info
- Any extra custom fields

Validation:
- Required fields cannot be empty
- Show error message if missing
- After submission:
  - Show **confirmation screen**
  - User can **edit before saving**

---

### Feature B — Store / View / Delete Projects (10%)

All project data must be stored in:

SQLite database (on device)

Users must be able to:

- View all projects
- Edit project
- Delete project
- Reset database

---

### Feature C — Manage Expenses (15%)

Each **Project can have multiple Expenses**.

Expense fields:

Required:
- Expense ID
- Date of Expense
- Amount
- Currency
- Expense Type
- Payment Method
- Claimant
- Payment Status

Expense Types:
- Travel
- Equipment
- Materials
- Services
- Software / Licenses
- Labour costs
- Utilities
- Miscellaneous

Payment Methods:
- Cash
- Credit Card
- Bank Transfer
- Cheque

Payment Status:
- Paid
- Pending
- Reimbursed

Optional:
- Description
- Location

Users must be able to:

- Add expense
- Edit expense
- Delete expense
- View expenses of a project

Data stored in SQLite.

---

### Feature D — Search Projects (10%)

Users can search projects by:

Basic Search:
- Project name
- Project description

Advanced Search:
- Date
- Status
- Owner

Expected behavior:

User types first letters → show matching results.

User can click result → open project details.

---

### Feature E — Upload to Cloud (15%)

Users can upload project data to a **cloud web service**.

Requirements:

- Upload all projects to server
- Check internet connection before uploading
- Handle connection errors

Optional improvement:

Synchronize local database changes with cloud database.

---

### Feature F — Additional Features (5%)

Optional improvements to Admin App.

Examples:

- Add photo using camera
- Auto-detect location (GPS)
- Better UI
- Charts / statistics
- Dark mode

---

# II. User App (Hybrid App)

Technology:
- React Native


---

### Feature G — Hybrid App for Users (10%)

User App must connect to **cloud service**.

Users can:

- View project list
- Search project by:
  - name
  - date

Data must come from cloud.

---

### Feature H — Favourite Projects (5%)

Users can mark projects as:

⭐ Favourite

Purpose:

Quick access to frequently used projects.

---

# PART B — REPORT (20%)

## Section 1 — Feature Checklist (2%)

Table showing:

| Feature | Status | Comment |
|-------|------|------|
| A | Complete | Used ConstraintLayout |
| B | Complete | SQLite database |
| C | Partial | UI built but storage not working |

Status options:

- Fully completed
- Partially completed
- Buggy
- Not implemented

---

## Section 2 — Reflection (350 words) (4%)

Discuss:

- How the apps were developed
- Lessons learned
- What went well
- What could be improved

---

## Section 3 — Evaluation (700–1000 words) (8%)

Evaluate the apps based on:

1. Human Computer Interaction (UX)
2. Security
3. Screen size compatibility
4. Improvements needed for production deployment

Must give **specific examples**.

---

## Section 4 — Screenshots (2%)

Screenshots demonstrating features implemented.

Add captions explaining:

- what feature is shown
- what the screen does

---

## Section 5 — Code Listing (2%)

Include code you wrote.

Example:

- Java classes
- API calls
- Database logic

Do NOT include auto-generated code.

---

# DEMONSTRATION VIDEO

Length:
≈ 10 minutes

Must show:

1. Admin Android App
2. Hybrid User App

Demo structure example:

1️⃣ Show project creation UI  
2️⃣ Show validation working  
3️⃣ Show project stored in database  
4️⃣ Show expense added  
5️⃣ Show search feature  
6️⃣ Show cloud upload  
7️⃣ Show hybrid app fetching projects  
8️⃣ Show favourite project feature  

For each feature:

Show **UI → Code → Result**

Example:

UI → create project  
Code → ProjectRepository.java  
Result → project appears in list

---

# FINAL SUBMISSION

Submit:

1️⃣ ZIP file
- Android project
- Hybrid app
- README instructions

2️⃣ Video demonstration

3️⃣ PDF report

---

# PASS REQUIREMENT

To pass (40%):

- At least **4 features implemented**
- Working demonstration
- Report mostly complete

---

# SAFE STRATEGY FOR 50–65%

Implement:

- A Project Creation
- B Database storage
- C Expense management
- D Search
- G Hybrid app basic

Skip complex sync if needed.