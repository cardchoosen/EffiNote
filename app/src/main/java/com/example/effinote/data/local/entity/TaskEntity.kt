package com.example.effinote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.effinote.domain.model.Task
import com.example.effinote.domain.model.TaskFrequency
import com.example.effinote.domain.model.TaskUnit

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val name: String,
    val frequency: String,
    val targetValue: Double,
    val unit: String,
    val customUnitName: String?,
    val stageCount: Int,
    val currentProgress: Double,
    val lastResetEpochDay: Long
) {
    fun toTask(): Task = Task(
        id = id,
        name = name,
        frequency = TaskFrequency.valueOf(frequency),
        targetValue = targetValue,
        unit = TaskUnit.valueOf(unit),
        customUnitName = customUnitName,
        stageCount = stageCount,
        currentProgress = currentProgress,
        lastResetEpochDay = lastResetEpochDay
    )

    companion object {
        fun fromTask(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            name = task.name,
            frequency = task.frequency.name,
            targetValue = task.targetValue,
            unit = task.unit.name,
            customUnitName = task.customUnitName,
            stageCount = task.stageCount,
            currentProgress = task.currentProgress,
            lastResetEpochDay = task.lastResetEpochDay
        )
    }
}
