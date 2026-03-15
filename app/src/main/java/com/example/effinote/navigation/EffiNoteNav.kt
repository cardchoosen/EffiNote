package com.example.effinote.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.effinote.ui.calendar.CalendarScreen
import com.example.effinote.ui.calendar.CalendarViewModel
import com.example.effinote.ui.createtask.CreateTaskScreen
import com.example.effinote.ui.createtask.CreateTaskViewModel
import com.example.effinote.ui.tasklist.TaskListScreen
import com.example.effinote.ui.tasklist.TaskListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val ROUTE_CALENDAR = "calendar"
const val ROUTE_TODAY = "today"
const val ROUTE_CREATE = "create"
const val ROUTE_EDIT_TASK = "edit_task/{taskId}"

fun NavHostController.navigateToCreateTask() = navigate(ROUTE_CREATE)
fun NavHostController.navigateToEditTask(taskId: String) = navigate("edit_task/$taskId")
fun NavHostController.navigateToToday() = navigate(ROUTE_TODAY) { popUpTo(ROUTE_TODAY) { inclusive = true } }

@Composable
fun EffiNoteNav(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val backStack = navController.currentBackStackEntryAsState()
    val current = backStack.value?.destination
    val selectedIndex = when {
        current?.route == ROUTE_CALENDAR -> 0
        current?.route?.startsWith("edit_task") == true -> 1
        current?.route == ROUTE_TODAY -> 1
        current?.route == ROUTE_CREATE -> 2
        else -> 1
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                listOf(
                    Triple(ROUTE_CALENDAR, Icons.Default.CalendarMonth, "日历"),
                    Triple(ROUTE_TODAY, Icons.Default.Today, "今日"),
                    Triple(ROUTE_CREATE, Icons.Default.Add, "创建")
                ).forEachIndexed { index, (route, icon, label) ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            when (route) {
                                ROUTE_CALENDAR -> navController.navigate(ROUTE_CALENDAR) { popUpTo(ROUTE_TODAY) { saveState = true }; launchSingleTop = true; restoreState = true }
                                ROUTE_TODAY -> navController.navigate(ROUTE_TODAY) { popUpTo(ROUTE_TODAY) { inclusive = false }; launchSingleTop = true }
                                ROUTE_CREATE -> navController.navigate(ROUTE_CREATE) { popUpTo(ROUTE_TODAY) { saveState = true }; launchSingleTop = true; restoreState = true }
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { paddingValues: PaddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = ROUTE_TODAY
        ) {
            composable(ROUTE_CALENDAR) {
                val vm: CalendarViewModel = viewModel()
                CalendarScreen(viewModel = vm)
            }
            composable(ROUTE_TODAY) {
                val listViewModel: TaskListViewModel = viewModel()
                TaskListScreen(
                    viewModel = listViewModel,
                    onAddTask = { navController.navigateToCreateTask() },
                    onTaskClick = { task -> navController.navigateToEditTask(task.id) }
                )
            }
            composable(ROUTE_CREATE) {
                val createViewModel: CreateTaskViewModel = viewModel()
                CreateTaskScreen(
                    viewModel = createViewModel,
                    existingTaskId = null,
                    onBack = { navController.navigateToToday() },
                    onSaved = { navController.navigateToToday() }
                )
            }
            composable(ROUTE_EDIT_TASK) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
                val createViewModel: CreateTaskViewModel = viewModel()
                val repo = com.example.effinote.data.repository.DefaultTaskRepository.instance
                LaunchedEffect(taskId) {
                    val task = withContext(Dispatchers.IO) { repo.getTaskById(taskId) }
                    createViewModel.initEdit(task)
                }
                CreateTaskScreen(
                    viewModel = createViewModel,
                    existingTaskId = taskId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.navigateToToday() }
                )
            }
        }
    }
}
