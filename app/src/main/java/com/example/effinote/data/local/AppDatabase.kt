package com.example.effinote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.effinote.data.local.dao.TaskDao
import com.example.effinote.data.local.dao.TaskPeriodRecordDao
import com.example.effinote.data.local.entity.TaskEntity
import com.example.effinote.data.local.entity.TaskPeriodRecordEntity

@Database(
    entities = [TaskEntity::class, TaskPeriodRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskPeriodRecordDao(): TaskPeriodRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "effinote.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
