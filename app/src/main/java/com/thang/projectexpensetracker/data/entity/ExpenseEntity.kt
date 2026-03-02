package com.thang.projectexpensetracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.thang.projectexpensetracker.data.entity.ProjectEntity

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val expenseId: Long = 0,
    val projectId: Long,
    val date: String,           // Date of Expense - Required
    val amount: Double,         // Amount - Required
    val currency: String,       // Currency - Required
    val type: String,           // Travel, Equipment... - Required
    val paymentMethod: String,  // Cash, Credit Card... - Required
    val claimant: String,       // Claimant - Required
    val paymentStatus: String,  // Paid, Pending... - Required
    val description: String? = null, // Optional
    val location: String? = null     // Optional
)