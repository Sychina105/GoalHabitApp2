package com.example.goalhabitapp.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.GoalDto
import com.example.goalhabitapp.data.remote.dto.GoalUpdateRequest
import com.example.goalhabitapp.data.repository.GoalsRepository
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun GoalsScreen(
    repo: GoalsRepository,
    onCreate: () -> Unit,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit,
    onSteps: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf<List<GoalDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    fun goalTypeLabel(type: String) = when (type) {
        "QUANT" -> "Количественная"
        "STEPS" -> "По шагам"
        "HABIT_AS_GOAL" -> "Привычка как цель"
        else -> type
    }

    fun statusLabel(s: String) = when (s) {
        "ACTIVE" -> "Активна"
        "PAUSED" -> "На паузе"
        "DONE" -> "Завершена"
        "CANCELED" -> "Отменена"
        else -> s
    }

    fun defaultDeltaFor(g: GoalDto): Int {
        return when (g.goalType) {
            "STEPS" -> 1000
            "QUANT" -> {
                // если цель маленькая — добавлять по 1, иначе по 5
                val t = g.targetValue ?: 0
                if (t in 1..10) 1 else 5
            }
            else -> 1
        }
    }

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                items = repo.list()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Цели", style = MaterialTheme.typography.headlineSmall)
            Row {
                TextButton(onClick = onBack) { Text("Назад") }
                TextButton(onClick = onCreate) { Text("+") }
            }
        }

        Spacer(Modifier.height(10.dp))

        info?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }

        when {
            loading -> Text("Загрузка...")
            error != null -> Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items, key = { it.id }) { g ->

                        var showAddDialog by remember(g.id) { mutableStateOf(false) }
                        var deltaText by remember(g.id) { mutableStateOf(defaultDeltaFor(g).toString()) }

                        Card {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {

                                Text(g.title, style = MaterialTheme.typography.titleMedium)

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    "Тип: ${goalTypeLabel(g.goalType)} | Статус: ${statusLabel(g.status)} | Приоритет: ${g.priority}"
                                )

                                Spacer(Modifier.height(6.dp))

                                val progressLine = buildString {
                                    append("Прогресс: ${g.progressValue}")
                                    g.targetValue?.let { append("/$it") }
                                    g.unit?.takeIf { it.isNotBlank() }?.let { append(" $it") }
                                }
                                Text(progressLine)

                                // Прогрессбар только если есть targetValue
                                val target = g.targetValue
                                if (target != null && target > 0) {
                                    Spacer(Modifier.height(6.dp))
                                    val p = (g.progressValue.toFloat() / target.toFloat()).coerceIn(0f, 1f)
                                    LinearProgressIndicator(
                                        progress = p,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text("${(p * 100f).roundToInt()}%")
                                }

                                g.deadline?.let {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Дедлайн: $it")
                                }

                                g.description?.takeIf { it.isNotBlank() }?.let {
                                    Spacer(Modifier.height(6.dp))
                                    Text(it)
                                }

                                Spacer(Modifier.height(10.dp))

                                // Переключатель "Показывать в профиле" — ВНУТРИ каждой карточки
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Показывать в профиле")
                                    Switch(
                                        checked = g.showInProfile,
                                        onCheckedChange = { checked ->
                                            scope.launch {
                                                try {
                                                    repo.update(
                                                        g.id,
                                                        GoalUpdateRequest(showInProfile = checked)
                                                    )
                                                    info = if (checked) "Цель добавлена в профиль ✅" else "Цель скрыта из профиля"
                                                    load()
                                                } catch (e: Exception) {
                                                    error = e.message
                                                }
                                            }
                                        }
                                    )
                                    if (g.goalType == "STEPS") {
                                        OutlinedButton(onClick = { onSteps(g.id) }) { Text("Шаги") }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // Кнопки
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showAddDialog = true },
                                        modifier = Modifier.weight(1f),
                                        enabled = g.status != "CANCELED"
                                    ) { Text("+ прогресс") }

                                    OutlinedButton(
                                        onClick = { onEdit(g.id) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Редактировать") }

                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    repo.delete(g.id)
                                                    info = "Цель удалена"
                                                    load()
                                                } catch (e: Exception) {
                                                    error = e.message
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Удалить") }
                                }
                            }
                        }

                        // Диалог добавления прогресса
                        if (showAddDialog) {
                            AlertDialog(
                                onDismissRequest = { showAddDialog = false },
                                title = { Text("Добавить прогресс") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            when (g.goalType) {
                                                "STEPS" -> "Например: 500, 1000, 3000"
                                                "QUANT" -> "Например: 1, 5, 10"
                                                else -> "Например: 1"
                                            }
                                        )
                                        OutlinedTextField(
                                            value = deltaText,
                                            onValueChange = { deltaText = it.filter(Char::isDigit) },
                                            label = { Text("Сколько добавить") },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number
                                            )
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val delta = deltaText.toIntOrNull() ?: 0
                                        if (delta <= 0) {
                                            error = "Введите число > 0"
                                            return@TextButton
                                        }
                                        scope.launch {
                                            try {
                                                repo.addProgress(g.id, delta) // <- должен быть в репозитории
                                                info = "Прогресс добавлен ✅"
                                                load()
                                            } catch (e: Exception) {
                                                error = e.message
                                            } finally {
                                                showAddDialog = false
                                            }
                                        }
                                    }) { Text("ОК") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showAddDialog = false }) { Text("Отмена") }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { load() }, modifier = Modifier.fillMaxWidth()) {
            Text("Обновить")
        }
    }
}