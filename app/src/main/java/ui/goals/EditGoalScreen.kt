package com.example.goalhabitapp.ui.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.GoalUpdateRequest
import com.example.goalhabitapp.data.repository.GoalsRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalScreen(
    goalId: Long,
    repo: GoalsRepository,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val goalTypeOptions = listOf(
        "Количественная" to "QUANT",
        "По шагам" to "STEPS",
        "Привычка как цель" to "HABIT_AS_GOAL"
    )
    var goalType by remember { mutableStateOf("QUANT") }

    var targetValueText by remember { mutableStateOf("") }
    val unitOptions = listOf("км", "книг", "часов", "дней", "раз", "страниц", "кг")
    var unit by remember { mutableStateOf(unitOptions.first()) }

    val statusOptions = listOf(
        "Активна" to "ACTIVE",
        "На паузе" to "PAUSED",
        "Завершена" to "DONE",
        "Отменена" to "CANCELED"
    )
    var status by remember { mutableStateOf("ACTIVE") }

    var priorityText by remember { mutableStateOf("3") }

    var showDatePicker by remember { mutableStateOf(false) }
    var deadline by remember { mutableStateOf<String?>(null) }

    fun goalTypeLabel(enumValue: String): String =
        goalTypeOptions.firstOrNull { it.second == enumValue }?.first ?: enumValue

    fun statusLabel(enumValue: String): String =
        statusOptions.firstOrNull { it.second == enumValue }?.first ?: enumValue

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                val g = repo.list().firstOrNull { it.id == goalId }
                if (g == null) {
                    error = "Цель не найдена"
                } else {
                    title = g.title
                    description = g.description ?: ""
                    goalType = g.goalType
                    targetValueText = g.targetValue?.toString() ?: ""
                    unit = g.unit ?: unitOptions.first()
                    deadline = g.deadline
                    priorityText = g.priority.toString()
                    status = g.status
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(goalId) { load() }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Редактировать цель", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("Назад") }
        }

        if (loading) {
            Text("Загрузка...")
            return@Column
        }

        error?.let {
            Text("Ошибка: $it", color = MaterialTheme.colorScheme.error)
            return@Column
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание (необязательно)") },
            modifier = Modifier.fillMaxWidth()
        )

        EnumDropdown(
            label = "Тип цели",
            currentLabel = goalTypeLabel(goalType),
            options = goalTypeOptions.map { it.first },
            onSelectIndex = { idx -> goalType = goalTypeOptions[idx].second }
        )

        val isQuant = goalType == "QUANT"
        if (isQuant) {
            OutlinedTextField(
                value = targetValueText,
                onValueChange = { targetValueText = it.filter(Char::isDigit) },
                label = { Text("Целевое значение") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            EnumDropdown(
                label = "Единица измерения",
                currentLabel = unit,
                options = unitOptions,
                onSelectIndex = { idx -> unit = unitOptions[idx] }
            )
        } else {
            targetValueText = ""
        }

        OutlinedTextField(
            value = deadline ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Дедлайн (необязательно)") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    focus.clearFocus()
                    showDatePicker = true
                },
            placeholder = { Text("Выбрать дату") }
        )

        OutlinedTextField(
            value = priorityText,
            onValueChange = { priorityText = it.filter(Char::isDigit) },
            label = { Text("Приоритет (1-5)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        EnumDropdown(
            label = "Статус",
            currentLabel = statusLabel(status),
            options = statusOptions.map { it.first },
            onSelectIndex = { idx -> status = statusOptions[idx].second }
        )

        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Отмена") }

            Button(
                onClick = {
                    saving = true
                    error = null
                    focus.clearFocus()

                    scope.launch {
                        try {
                            val t = title.trim()
                            if (t.isEmpty()) {
                                error = "Введите название"
                                return@launch
                            }

                            val pr = priorityText.toIntOrNull() ?: 3
                            val targetValue = if (isQuant) targetValueText.toIntOrNull() else null
                            if (isQuant && (targetValue == null || targetValue <= 0)) {
                                error = "Введите корректное целевое значение"
                                return@launch
                            }

                            repo.update(
                                goalId,
                                GoalUpdateRequest(
                                    title = t,
                                    description = description.trim().ifBlank { null },
                                    goalType = goalType,
                                    targetValue = targetValue,
                                    unit = if (isQuant) unit else null,
                                    deadline = deadline,
                                    priority = pr,
                                    status = status
                                )
                            )
                            onDone()
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            saving = false
                        }
                    }
                },
                enabled = !saving,
                modifier = Modifier.weight(1f)
            ) { Text(if (saving) "..." else "Сохранить") }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    deadline = millis?.let { millisToIsoDate(it) }
                    showDatePicker = false
                }) { Text("ОК") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnumDropdown(
    label: String,
    currentLabel: String,
    options: List<String>,
    onSelectIndex: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { idx, item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onSelectIndex(idx)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun millisToIsoDate(millis: Long): String {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return date.toString()
}
