package com.example.effinote.domain.model

/**
 * 任务循环频率：每日 / 每周 / 每月
 */
enum class TaskFrequency(val displayName: String) {
    DAILY("每日"),
    WEEKLY("每周"),
    MONTHLY("每月")
}
