package com.example.effinote.ui.createtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.effinote.data.repository.DefaultTaskRepository
import com.example.effinote.data.repository.TaskRepository
import com.example.effinote.domain.model.Task
import com.example.effinote.domain.model.TaskFrequency
import com.example.effinote.domain.model.TaskUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

data class CreateTaskState(
    val name: String = "",
    val frequency: TaskFrequency = TaskFrequency.DAILY,
    val targetValue: String = "",
    val unit: TaskUnit = TaskUnit.ML,
    val customUnitName: String = "",
    val stageCount: Int = 4
) {
    val targetValueDouble: Double get() = targetValue.toDoubleOrNull() ?: 0.0
    val valuePerStage: Double get() = if (stageCount <= 0) 0.0 else targetValueDouble / stageCount
}

class CreateTaskViewModel(
    private val repository: TaskRepository = DefaultTaskRepository.instance
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTaskState())
    val state = _state.asStateFlow()

    fun initEdit(task: Task?) {
        if (task == null) return
        _state.update {
            it.copy(
                name = task.name,
                frequency = task.frequency,
                targetValue = task.targetValue.toInt().toString(),
                unit = task.unit,
                customUnitName = task.customUnitName ?: "",
                stageCount = task.stageCount
            )
        }
    }

    fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun updateFrequency(frequency: TaskFrequency) {
        _state.update { it.copy(frequency = frequency) }
    }

    fun updateTargetValue(value: String) {
        _state.update { it.copy(targetValue = value) }
    }

    fun updateUnit(unit: TaskUnit, customName: String = "") {
        _state.update { it.copy(unit = unit, customUnitName = customName) }
    }

    fun updateStageCount(count: Int) {
        _state.update { it.copy(stageCount = count.coerceIn(1, 20)) }
    }

    fun save(existingTaskId: String? = null, onSaved: () -> Unit) {
        val s = _state.value
        val target = s.targetValueDouble
        if (s.name.isBlank() || target <= 0 || s.stageCount < 1) return

        viewModelScope.launch {
            val epochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
            val task = Task(
                id = existingTaskId ?: UUID.randomUUID().toString(),
                name = s.name.trim(),
                frequency = s.frequency,
                targetValue = target,
                unit = s.unit,
                customUnitName = s.customUnitName.takeIf { it.isNotBlank() },
                stageCount = s.stageCount,
                currentProgress = 0.0,
                lastResetEpochDay = epochDay
            )
            if (existingTaskId != null) {
                val existing = repository.getTaskById(existingTaskId)
                repository.updateTask(
                    task.copy(
                        currentProgress = existing?.currentProgress ?: 0.0,
                        lastResetEpochDay = existing?.lastResetEpochDay ?: epochDay
                    )
                )
            } else {
                repository.addTask(task)
            }
            onSaved()
        }
    }
}
