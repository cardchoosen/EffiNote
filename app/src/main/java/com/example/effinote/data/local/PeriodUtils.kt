package com.example.effinote.data.local

import com.example.effinote.domain.model.TaskFrequency
import java.util.Calendar
import java.util.TimeZone

private val UTC = TimeZone.getTimeZone("UTC")

object PeriodUtils {
    fun millisToEpochDay(millis: Long): Long = millis / (24 * 60 * 60 * 1000)

    /** 当前时间的纪元日 */
    fun currentEpochDay(): Long = millisToEpochDay(System.currentTimeMillis())

    /** 给定某天的纪元日，返回该天所属周期的周期起始纪元日 */
    fun periodEpochDayFor(dayEpochDay: Long, frequency: TaskFrequency): Long {
        val cal = Calendar.getInstance(UTC)
        cal.timeInMillis = dayEpochDay * 24L * 60 * 60 * 1000
        when (frequency) {
            TaskFrequency.DAILY -> return dayEpochDay
            TaskFrequency.WEEKLY -> {
                val dow = cal.get(Calendar.DAY_OF_WEEK)
                val sunOffset = dow - Calendar.SUNDAY
                cal.add(Calendar.DAY_OF_MONTH, -sunOffset)
                return millisToEpochDay(cal.timeInMillis)
            }
            TaskFrequency.MONTHLY -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                return millisToEpochDay(cal.timeInMillis)
            }
        }
    }

    fun currentPeriodEpochDay(frequency: TaskFrequency): Long =
        periodEpochDayFor(currentEpochDay(), frequency)
}
