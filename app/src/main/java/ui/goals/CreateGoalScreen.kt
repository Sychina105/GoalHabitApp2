package com.example.goalhabitapp.ui.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.GoalCreateRequest
import com.example.goalhabitapp.data.repository.GoalsRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalScreen(
    repo: GoalsRepository,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current

    // Красивые поля
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Тип цели (показываем по-русски, отправляем enum)
    val goalTypeOptions = listOf(
        "Количественная" to "QUANT",
        "По шагам" to "STEPS",
        "Привычка как цель" to "HABIT_AS_GOAL"
    )
    var goalType by remember { mutableStateOf(goalTypeOptions.first().second) } // enum

    // Кол-во (только для QUANT)
    var targetValueText by remember { mutableStateOf("") }

    // Unit dropdown (для QUANT)
    val unitOptions = listOf("км", "книг", "часов", "дней", "раз", "страниц", "кг")
    var unit by remember { mutableStateOf(unitOptions.first()) }

    // Статус dropdown
    val statusOptions = listOf(
        "Активна" to "ACTIVE",
        "На паузе" to "PAUSED",
        "Завершена" to "DONE",
        "Отменена" to "CANCELED"
    )
    var status by remember { mutableStateOf("ACTIVE") }

    // Дедлайн через DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    var deadline by remember { mutableStateOf<String?>(null) } // "YYYY-MM-DD" или null

    // Приоритет — скрываем (по умолчанию 3)
    val priorityDefault = 3

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun goalTypeLabel(enumValue: String): String =
        goalTypeOptions.first { it.second == enumValue }.first

    fun statusLabel(enumValue: String): String =
        statusOptions.first { it.second == enumValue }.first

    // ---------- UI ----------
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Новая цель", style = MaterialTheme.typography.headlineSmall)

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

        // Тип цели (dropdown)
        EnumDropdown(
            label = "Тип цели",
            currentLabel = goalTypeLabel(goalType),
            options = goalTypeOptions.map { it.first },
            onSelectIndex = { idx -> goalType = goalTypeOptions[idx].second }
        )

        // Кол-во + единица показываем только для QUANT
        if (goalType == "QUANT") {
            OutlinedTextField(
                value = targetValueText,
                onValueChange = { new ->
                    // оставляем только цифры
                    targetValueText = new.filter { it.isDigit() }
                },
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
            // чтобы не путаться: чистим “количество”, если тип не QUANT
            targetValueText = ""
        }

        // Дедлайн (DatePicker)
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

        // Статус (dropdown)
        EnumDropdown(
            label = "Статус",
            currentLabel = statusLabel(status),
            options = statusOptions.map { it.first },
            onSelectIndex = { idx -> status = statusOptions[idx].second }
        )

        error?.let { Text("Ошибка: $it", color = MaterialTheme.colorScheme.error) }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Назад") }

            Button(
                onClick = {
                    loading = true
                    error = null
                    focus.clearFocus()

                    scope.launch {
                        try {
                            val isQuant = goalType == "QUANT"
                            val targetValue = if (isQuant) targetValueText.toIntOrNull() else null

                            // простая валидация
                            if (title.trim().isEmpty()) {
                                error = "Введите название цели"
                                return@launch
                            }
                            if (isQuant && (targetValue == null || targetValue <= 0)) {
                                error = "Введите корректное целевое значение"
                                return@launch
                            }

                            val req = GoalCreateRequest(
                                title = title.trim(),
                                description = description.trim().ifBlank { null },
                                goalType = goalType,
                                targetValue = targetValue,
                                unit = if (isQuant) unit else null,
                                deadline = deadline,
                                priority = priorityDefault,
                                status = status
                            )

                            repo.create(req)
                            onDone()
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) { Text(if (loading) "..." else "Создать") }
        }
    }

    // ---------- DatePicker Dialog ----------
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
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
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
    val date = Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    // YYYY-MM-DD
    return date.toString()
}
