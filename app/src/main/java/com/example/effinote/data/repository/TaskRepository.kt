package com.example.effinote.data.repository

import com.example.effinote.data.local.PeriodUtils
import com.example.effinote.data.local.dao.TaskDao
import com.example.effinote.data.local.dao.TaskPeriodRecordDao
import com.example.effinote.data.local.entity.TaskEntity
import com.example.effinote.data.local.entity.TaskPeriodRecordEntity
import com.example.effinote.domain.model.Task
import com.example.effinote.domain.model.TaskFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 某日某任务的完成情况（供日历等展示） */
data class DayRecordView(
    val taskId: String,
    val taskName: String,
    val progress: Double,
    val targetValue: Double,
    val completed: Boolean
)

class TaskRepository(
    private val taskDao: TaskDao,
    private val recordDao: TaskPeriodRecordDao
) {
    val tasks: Flow<List<Task>> = taskDao.observeAll().map { list -> list.map { it.toTask() } }

    suspend fun getTaskById(id: String): Task? = taskDao.getById(id)?.toTask()

    suspend fun isEmpty(): Boolean = taskDao.count() == 0

    suspend fun addTask(task: Task) {
        val toInsert = if (task.lastResetEpochDay <= 0)
            task.copy(lastResetEpochDay = PeriodUtils.currentPeriodEpochDay(task.frequency))
        else task
        taskDao.insert(TaskEntity.fromTask(toInsert))
    }

    suspend fun updateTask(task: Task) {
        taskDao.update(TaskEntity.fromTask(task))
    }

    suspend fun deleteTask(id: String) {
        taskDao.deleteById(id)
    }

    suspend fun addProgress(taskId: String, amount: Double) {
        val entity = taskDao.getById(taskId) ?: return
        val task = entity.toTask()
        val newProgress = (task.currentProgress + amount).coerceAtMost(task.targetValue)
        taskDao.update(entity.copy(currentProgress = newProgress))
    }

    /** 检测周期是否更替：若更替则归档上一周期并重置进度（与多数打卡 App 一致：进入前台时检查） */
    suspend fun checkAndRollPeriod() {
        val tasksList = taskDao.getAll()
        for (entity in tasksList) {
            val task = entity.toTask()
            val currentPeriod = PeriodUtils.currentPeriodEpochDay(task.frequency)
            if (entity.lastResetEpochDay < currentPeriod) {
                if (entity.lastResetEpochDay > 0) {
                    recordDao.insert(
                        TaskPeriodRecordEntity(
                            taskId = task.id,
                            frequency = task.frequency.name,
                            periodEpochDay = entity.lastResetEpochDay,
                            progress = entity.currentProgress,
                            targetValue = entity.targetValue,
                            completed = entity.currentProgress >= entity.targetValue
                        )
                    )
                }
                taskDao.update(entity.copy(currentProgress = 0.0, lastResetEpochDay = currentPeriod))
            }
        }
    }

    /** 某天各任务的完成记录（用于日历）；若是当天则含当前未归档进度 */
    suspend fun getRecordsForDay(dayEpochDay: Long): List<DayRecordView> {
        val entities = taskDao.getAll()
        val today = PeriodUtils.currentEpochDay()
        val result = mutableListOf<DayRecordView>()
        for (entity in entities) {
            val task = entity.toTask()
            val period = PeriodUtils.periodEpochDayFor(dayEpochDay, task.frequency)
            val record = recordDao.get(entity.id, entity.frequency, period)
            if (record != null) {
                result.add(DayRecordView(entity.id, entity.name, record.progress, record.targetValue, record.completed))
            } else if (dayEpochDay == today && period == PeriodUtils.currentPeriodEpochDay(task.frequency)) {
                result.add(DayRecordView(entity.id, entity.name, entity.currentProgress, entity.targetValue, entity.currentProgress >= entity.targetValue))
            }
        }
        return result
    }

    /** 某月内每天的记录（用于日历月视图：逐日查询） */
    suspend fun getRecordsForMonth(year: Int, month: Int): Map<Long, List<DayRecordView>> {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val maxDay = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val result = mutableMapOf<Long, List<DayRecordView>>()
        for (dayOfMonth in 1..maxDay) {
            cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
            val dayEpoch = PeriodUtils.millisToEpochDay(cal.timeInMillis)
            result[dayEpoch] = getRecordsForDay(dayEpoch)
        }
        return result
    }
}
