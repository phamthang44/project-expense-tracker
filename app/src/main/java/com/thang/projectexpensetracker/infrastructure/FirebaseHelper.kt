package com.thang.projectexpensetracker.infrastructure

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import com.thang.projectexpensetracker.util.NetworkUtils
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Low-level Firestore helper — suspend-based operations for
 * projects and expenses.  Doc IDs are deterministic so repeated
 * uploads act as upserts.
 */
object FirebaseHelper {
    private val db = Firebase.firestore

    // ── Batch uploads (used by full sync) ──────────────────────────────────

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

    // ── Individual project operations ──────────────────────────────────────

    /** Upsert a single project to Firestore. */
    suspend fun upsertProject(project: ProjectEntity): Boolean =
        suspendCoroutine { cont ->
            db.collection("projects").document(project.projectCode)
                .set(project)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }

    /** Delete a single project from Firestore by its projectCode. */
    suspend fun deleteProject(projectCode: String): Boolean =
        suspendCoroutine { cont ->
            db.collection("projects").document(projectCode)
                .delete()
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }

    // ── Individual expense operations ──────────────────────────────────────

    /** Upsert a single expense to Firestore. */
    suspend fun upsertExpense(expense: ExpenseEntity): Boolean =
        suspendCoroutine { cont ->
            db.collection("expenses")
                .document("${expense.projectId}_${expense.expenseId}")
                .set(expense)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }

    /** Delete a single expense from Firestore. */
    suspend fun deleteExpense(projectId: Long, expenseId: Long): Boolean =
        suspendCoroutine { cont ->
            db.collection("expenses")
                .document("${projectId}_${expenseId}")
                .delete()
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }

    /** Delete all expenses for a given project from Firestore. */
    suspend fun deleteExpensesByProject(projectId: Long): Boolean =
        suspendCoroutine { cont ->
            db.collection("expenses")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        cont.resume(true)
                        return@addOnSuccessListener
                    }
                    val batch = db.batch()
                    snapshot.documents.forEach { batch.delete(it.reference) }
                    batch.commit()
                        .addOnSuccessListener { cont.resume(true) }
                        .addOnFailureListener { cont.resume(false) }
                }
                .addOnFailureListener { cont.resume(false) }
        }
}