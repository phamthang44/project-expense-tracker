package com.thang.projectexpensetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectCode: String,
    val projectName: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val manager: String,
    val status: String,
    val budget: Double,
    val specialRequirements: String? = null,
    val clientInfo: String? = null,
    val priority: String = "Normal"
)