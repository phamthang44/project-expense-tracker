package com.thang.projectexpensetracker.infrastructure

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Low-level Firestore helper — suspend-based batch upload for
 * projects and expenses.  Doc IDs are deterministic so repeated
 * uploads act as upserts.
 */
object FirebaseHelper {
    private val db = Firebase.firestore

    /** Upload all projects as a batch.  Doc ID = projectCode. */
    suspend fun uploadProjects(projects: List<ProjectEntity>): Boolean {
        if (projects.isEmpty()) return true
        return suspendCoroutine { cont ->
            val batch = db.batch()
            projects.forEach { project ->
                val docRef = db.collection("projects").document(project.projectCode)
                batch.set(docRef, project)
            }
            batch.commit()
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }
    }

    /** Upload all expenses as a batch.  Doc ID = "projectId_expenseId". */
    suspend fun uploadExpenses(expenses: List<ExpenseEntity>): Boolean {
        if (expenses.isEmpty()) return true
        return suspendCoroutine { cont ->
            val batch = db.batch()
            expenses.forEach { expense ->
                val docRef = db.collection("expenses")
                    .document("${expense.projectId}_${expense.expenseId}")
                batch.set(docRef, expense)
            }
            batch.commit()
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }
    }
}