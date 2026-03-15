package com.example.effinote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.effinote.data.local.entity.TaskPeriodRecordEntity

@Dao
interface TaskPeriodRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TaskPeriodRecordEntity)

    @Query("SELECT * FROM task_period_records WHERE taskId = :taskId AND frequency = :frequency AND periodEpochDay = :periodEpochDay LIMIT 1")
    suspend fun get(taskId: String, frequency: String, periodEpochDay: Long): TaskPeriodRecordEntity?

    @Query("SELECT * FROM task_period_records WHERE periodEpochDay >= :fromDay AND periodEpochDay <= :toDay ORDER BY periodEpochDay")
    suspend fun getRecordsBetween(fromDay: Long, toDay: Long): List<TaskPeriodRecordEntity>
}
