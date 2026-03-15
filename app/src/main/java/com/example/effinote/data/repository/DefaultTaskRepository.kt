package com.example.effinote.data.repository

import android.content.Context
import com.example.effinote.data.local.AppDatabase

/**
 * 应用内共享的默认任务仓库（ViewModel / Widget 共用），需在 Application 中 init。
 */
object DefaultTaskRepository {
    @Volatile
    private var _instance: TaskRepository? = null

    val instance: TaskRepository
        get() = _instance ?: error("DefaultTaskRepository not initialized. Call init(context) in Application.")

    fun init(context: Context) {
        if (_instance != null) return
        val db = AppDatabase.getInstance(context)
        _instance = TaskRepository(db.taskDao(), db.taskPeriodRecordDao())
    }
}
