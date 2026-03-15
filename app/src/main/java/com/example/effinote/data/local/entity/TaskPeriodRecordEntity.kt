package com.example.effinote.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/** 某一周期内的任务完成记录（用于历史与日历展示） */
@Entity(
    tableName = "task_period_records",
    primaryKeys = ["taskId", "frequency", "periodEpochDay"],
    indices = [Index(value = ["periodEpochDay"]), Index(value = ["taskId"])]
)
data class TaskPeriodRecordEntity(
    val taskId: String,
    val frequency: String,
    /** 周期起始日（纪元日）：每日=当天；每周=该周首日；每月=该月 1 日） */
    val periodEpochDay: Long,
    val progress: Double,
    val targetValue: Double,
    val completed: Boolean
)
