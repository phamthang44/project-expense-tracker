package com.thang.projectexpensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.thang.projectexpensetracker.data.dao.ExpenseDao
import com.thang.projectexpensetracker.data.dao.ProjectDao
import com.thang.projectexpensetracker.data.entity.ExpenseEntity
import com.thang.projectexpensetracker.data.entity.ProjectEntity

@Database(entities = [ProjectEntity::class, ExpenseEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "projectexpensetracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}