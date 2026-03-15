package com.example.effinote.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.effinote.data.local.PeriodUtils
import com.example.effinote.data.repository.DayRecordView
import com.example.effinote.data.repository.DefaultTaskRepository
import com.example.effinote.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

data class MonthViewState(
    val year: Int,
    val month: Int,
    val dayRecords: Map<Long, List<DayRecordView>> = emptyMap(),
    val todayEpochDay: Long = 0L
)

class CalendarViewModel(
    private val repository: TaskRepository = DefaultTaskRepository.instance
) : ViewModel() {

    private val _state = MutableStateFlow(
        MonthViewState(
            year = currentYear(),
            month = currentMonth(),
            todayEpochDay = PeriodUtils.currentEpochDay()
        )
    )
    val state = _state.asStateFlow()

    init {
        loadMonth(_state.value.year, _state.value.month)
    }

    fun setMonth(year: Int, month: Int) {
        _state.update { it.copy(year = year, month = month) }
        loadMonth(year, month)
    }

    fun loadMonth(year: Int, month: Int) {
        viewModelScope.launch {
            val dayRecords = repository.getRecordsForMonth(year, month)
            _state.update {
                it.copy(
                    year = year,
                    month = month,
                    dayRecords = dayRecords,
                    todayEpochDay = PeriodUtils.currentEpochDay()
                )
            }
        }
    }

    fun goPrevMonth() {
        val (y, m) = prevMonth(_state.value.year, _state.value.month)
        setMonth(y, m)
    }

    fun goNextMonth() {
        val (y, m) = nextMonth(_state.value.year, _state.value.month)
        setMonth(y, m)
    }

    private fun currentYear(): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        return cal.get(Calendar.YEAR)
    }

    private fun currentMonth(): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        return cal.get(Calendar.MONTH) + 1
    }

    private fun prevMonth(year: Int, month: Int): Pair<Int, Int> {
        if (month == 1) return year - 1 to 12
        return year to month - 1
    }

    private fun nextMonth(year: Int, month: Int): Pair<Int, Int> {
        if (month == 12) return year + 1 to 1
        return year to month + 1
    }
}
