package com.example.effinote.ui.createtask

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.effinote.domain.model.TaskFrequency
import com.example.effinote.domain.model.TaskUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    viewModel: CreateTaskViewModel,
    existingTaskId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = { Text(if (existingTaskId != null) "编辑任务" else "新建任务") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        viewModel.save(existingTaskId) {
                            onSaved()
                        }
                    }
                ) {
                    Text("保存", color = MaterialTheme.colorScheme.primary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // 任务名称
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text("任务名称") },
                placeholder = { Text("例如：每日饮水") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 频率
            Text(
                text = "频率",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFrequency.entries.forEach { freq ->
                    FilterChip(
                        selected = state.frequency == freq,
                        onClick = { viewModel.updateFrequency(freq) },
                        label = { Text(freq.displayName) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 量化目标
            Text(
                text = "量化目标",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = state.targetValue,
                onValueChange = viewModel::updateTargetValue,
                placeholder = { Text("例如 2000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            UnitDropdown(
                selectedUnit = state.unit,
                customName = state.customUnitName,
                onUnitSelected = { u, custom -> viewModel.updateUnit(u, custom) }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 阶段划分
            Text(
                text = "阶段划分",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "将总量分为 ${state.stageCount} 段，每段 ${formatStageValue(state.valuePerStage)} ${TaskUnit.displayNameFor(state.unit, state.customUnitName.takeIf { it.isNotBlank() })}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Slider(
                value = state.stageCount.toFloat(),
                onValueChange = { viewModel.updateStageCount(it.toInt()) },
                valueRange = 1f..20f,
                steps = 18,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1 段", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text("20 段", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun UnitDropdown(
    selectedUnit: TaskUnit,
    customName: String,
    onUnitSelected: (TaskUnit, String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskUnit.presetUnits.forEach { unit ->
            FilterChip(
                selected = selectedUnit == unit,
                onClick = { onUnitSelected(unit, "") },
                label = { Text(unit.displayName) }
            )
        }
    }
}


private fun formatStageValue(value: Double): String {
    return when {
        value >= 1000 -> "${(value / 1000).toInt()}k"
        value == value.toLong().toDouble() -> value.toLong().toString()
        else -> "%.1f".format(value)
    }
}
