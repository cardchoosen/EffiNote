package com.example.effinote.domain.model

/**
 * 预设单位，支持自定义（通过 displayName 传入）
 */
enum class TaskUnit(val displayName: String) {
    COUNT("个"),
    TIMES("次"),
    SETS("组"),
    MINUTES("分钟"),
    METERS("米"),
    ML("毫升"),
    PAGES("页"),
    CUSTOM(""); // 自定义时由调用方传入 displayName

    companion object {
        val presetUnits: List<TaskUnit> = listOf(COUNT, TIMES, SETS, MINUTES, METERS, ML, PAGES)

        fun displayNameFor(unit: TaskUnit, customName: String? = null): String =
            if (unit == CUSTOM && !customName.isNullOrBlank()) customName else unit.displayName
    }
}
