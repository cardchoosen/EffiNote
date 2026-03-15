package com.example.effinote.debug

import android.content.Context
import com.example.effinote.data.repository.DefaultTaskRepository
import com.example.effinote.domain.model.Task
import com.example.effinote.domain.model.TaskFrequency
import com.example.effinote.domain.model.TaskUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.UUID

/**
 * 开发用：从 assets/debug/init_data_test.json 读取测试数据并写入数据库。
 * 若文件为空或不存在则跳过；仅在数据库无任务时插入。
 */
object DebugInitDataLoader {
    private const val ASSET_PATH = "debug/init_data_test.json"

    fun loadIfNeeded(context: Context) {
        runBlocking {
            val repo = DefaultTaskRepository.instance
            if (!repo.isEmpty()) return@runBlocking
            val jsonStr = runCatching {
                withContext(Dispatchers.IO) {
                    context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
                }
            }.getOrNull() ?: return@runBlocking
            if (jsonStr.isBlank()) return@runBlocking
            val array = runCatching { JSONArray(jsonStr) }.getOrNull() ?: return@runBlocking
            if (array.length() == 0) return@runBlocking
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val name = obj.optString("name").takeIf { it.isNotBlank() } ?: continue
                val freqStr = obj.optString("frequency", "DAILY")
                val freq = TaskFrequency.entries.find { it.name.equals(freqStr, true) } ?: TaskFrequency.DAILY
                val target = obj.optDouble("targetValue", 0.0)
                if (target <= 0) continue
                val unitStr = obj.optString("unit", "ML")
                val unit = TaskUnit.entries.find { it.name.equals(unitStr, true) } ?: TaskUnit.ML
                val stageCount = obj.optInt("stageCount", 4).coerceIn(1, 20)
                val task = Task(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    frequency = freq,
                    targetValue = target,
                    unit = unit,
                    customUnitName = obj.optString("customUnitName").takeIf { it.isNotBlank() },
                    stageCount = stageCount,
                    currentProgress = 0.0,
                    lastResetEpochDay = 0L
                )
                repo.addTask(task)
            }
        }
    }
}
