package com.thang.projectexpensetracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.thang.projectexpensetracker.data.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    /** One-shot list for sync upload (non-Flow). */
    @Query("SELECT * FROM projects")
    suspend fun getAllProjectsList(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE projectName LIKE :query OR description LIKE :query")
    fun searchProjects(query: String): Flow<List<ProjectEntity>>

    @Insert
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects()

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ProjectEntity?

    /**
     * Advanced search – filters by name/code/description text, status, owner (manager),
     * and an optional date range.
     *
     * Date comparison is done on the ISO-formatted date strings (yyyy-MM-dd) that the
     * ViewModel converts from the display format (dd/MM/yyyy) before calling this query,
     * so lexicographic ordering works correctly.
     *
     * Any parameter set to NULL is treated as "no filter" (match all).
     */
    @Query("""
        SELECT * FROM projects
        WHERE (
            projectName   LIKE :query OR
            description   LIKE :query OR
            projectCode   LIKE :query OR
            manager       LIKE :query
        )
        AND (:status IS NULL OR status = :status)
        AND (:owner  IS NULL OR manager LIKE :owner)
        AND (:startAfter IS NULL OR startDate >= :startAfter)
        AND (:endBefore  IS NULL OR endDate   <= :endBefore)
        ORDER BY
            CASE status
                WHEN 'Active'    THEN 0
                WHEN 'On Hold'   THEN 1
                WHEN 'Completed' THEN 2
                ELSE 3
            END,
            startDate DESC
    """)
    fun advancedSearch(
        query: String,
        status: String?,
        owner: String?,
        startAfter: String?,
        endBefore: String?
    ): Flow<List<ProjectEntity>>
}