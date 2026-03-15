package com.example.effinote.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.effinote.data.repository.DefaultTaskRepository
import com.example.effinote.data.repository.TaskRepository
import com.example.effinote.domain.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskListViewModel(
    private val repository: TaskRepository = DefaultTaskRepository.instance
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addProgress(taskId: String, amount: Double) {
        viewModelScope.launch { repository.addProgress(taskId, amount) }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch { repository.deleteTask(id) }
    }
}
