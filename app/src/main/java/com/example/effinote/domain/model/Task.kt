package com.example.effinote.domain.model

import java.util.UUID

/**
 * 阶段式量化任务
 * @param id 唯一标识
 * @param name 任务名称
 * @param frequency 循环频率
 * @param targetValue 目标总量（如 2000ml、50 个）
 * @param unit 单位
 * @param customUnitName 自定义单位名称（当 unit == CUSTOM 时使用）
 * @param stageCount 阶段数 n，系统自动计算每阶段增量 = targetValue / stageCount
 * @param currentProgress 当前已完成量（本周期内）
 * @param lastResetEpochDay 上次重置时的“纪元日”，用于判断是否需要按频率重置
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val frequency: TaskFrequency,
    val targetValue: Double,
    val unit: TaskUnit,
    val customUnitName: String? = null,
    val stageCount: Int,
    val currentProgress: Double = 0.0,
    val lastResetEpochDay: Long = 0L
) {
    /** 每阶段需完成的量 */
    val valuePerStage: Double
        get() = if (stageCount <= 0) targetValue else targetValue / stageCount

    /** 当前处于第几阶段（1-based），未开始为 1 */
    val currentStage: Int
        get() {
            if (currentProgress >= targetValue) return stageCount
            val stage = (currentProgress / valuePerStage).toInt()
            return (stage + 1).coerceIn(1, stageCount)
        }

    /** 当前阶段还需完成多少即可进入下一阶段 */
    val remainingInCurrentStage: Double
        get() {
            if (currentProgress >= targetValue) return 0.0
            val progressInStage = currentProgress % valuePerStage
            return valuePerStage - progressInStage
        }

    /** 当前阶段的目标增量（用于 Widget 按钮显示，如 +500） */
    val currentStageTargetValue: Double
        get() = valuePerStage

    /** 总进度百分比 0..1 */
    val progressPercent: Float
        get() = (currentProgress / targetValue).toFloat().coerceIn(0f, 1f)

    /** 是否已完成本周期目标 */
    val isCompleted: Boolean
        get() = currentProgress >= targetValue

    /** 单位显示名 */
    fun unitDisplayName(): String = TaskUnit.displayNameFor(unit, customUnitName)
}
