package com.thang.projectexpensetracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity) //

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity) //

    @Query("SELECT * FROM expenses WHERE projectId = :projectId")
    fun getExpensesByProject(projectId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE expenseId = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    /** Returns a map of projectId → SUM(amount) for every project that has expenses. */
    @Query("SELECT projectId, SUM(amount) AS total FROM expenses GROUP BY projectId")
    fun getTotalsByProject(): Flow<Map<@MapColumn("projectId") Long, @MapColumn("total") Double>>


}