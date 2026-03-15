package com.example.effinote.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.effinote.data.local.PeriodUtils
import com.example.effinote.data.repository.DayRecordView
import java.util.Calendar
import java.util.TimeZone

private val UTC = TimeZone.getTimeZone("UTC")

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel
) {
    val state by viewModel.state.collectAsState()
    val cal = Calendar.getInstance(UTC).apply {
        set(state.year, state.month - 1, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val leadingBlanks = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7
    val totalCells = leadingBlanks + maxDay

    val minDrag = 80f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(viewModel) {
                var total = 0f
                detectVerticalDragGestures { _, dragAmount ->
                    total += dragAmount
                    if (kotlin.math.abs(total) >= minDrag) {
                        if (total > 0) viewModel.goNextMonth() else viewModel.goPrevMonth()
                        total = 0f
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.goPrevMonth() }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "上月")
            }
            Text(
                text = "${state.year}年${state.month}月",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = { viewModel.goNextMonth() }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "下月")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(leadingBlanks) {
                item { Box(Modifier.aspectRatio(1f)) }
            }
            val todayEpoch = state.todayEpochDay
            for (dayOfMonth in 1..maxDay) {
                val cellCal = Calendar.getInstance(UTC).apply {
                    set(state.year, state.month - 1, dayOfMonth)
                }
                val dayEpoch = PeriodUtils.millisToEpochDay(cellCal.timeInMillis)
                val records = state.dayRecords[dayEpoch] ?: emptyList()
                val isPast = dayEpoch < todayEpoch
                val isToday = dayEpoch == todayEpoch
                val isFuture = dayEpoch > todayEpoch
                item(key = "day_$dayOfMonth") {
                    DayCell(
                        dayOfMonth = dayOfMonth,
                        records = records,
                        isPast = isPast,
                        isToday = isToday,
                        isFuture = isFuture
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    dayOfMonth: Int,
    records: List<DayRecordView>,
    isPast: Boolean,
    isToday: Boolean,
    isFuture: Boolean
) {
    val alpha = when {
        isFuture -> 0.4f
        else -> 1f
    }
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
                }
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
        )
        if (records.isNotEmpty()) {
            val completed = records.count { it.completed }
            val total = records.size
            Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(minOf(total, 3)) { i ->
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .padding(1.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < completed)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}
