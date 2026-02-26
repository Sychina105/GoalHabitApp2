package com.example.goalhabitapp.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.GoalStepDto
import com.example.goalhabitapp.data.repository.GoalsRepository
import kotlinx.coroutines.launch

@Composable
fun GoalStepsScreen(
    goalId: Long,
    repo: GoalsRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var steps by remember { mutableStateOf<List<GoalStepDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var newTitle by remember { mutableStateOf("") }

    // редактирование шага
    var editStep by remember { mutableStateOf<GoalStepDto?>(null) }
    var editText by remember { mutableStateOf("") }

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                steps = repo.steps(goalId)
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(goalId) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Шаги цели", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("Назад") }
        }

        Spacer(Modifier.height(12.dp))

        error?.let {
            Text("Ошибка: $it", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        // Добавление шага
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("Новый шаг") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val t = newTitle.trim()
                    if (t.isEmpty()) return@Button
                    scope.launch {
                        try {
                            repo.addStep(goalId, t)
                            newTitle = ""
                            load()
                        } catch (e: Exception) {
                            error = e.message
                        }
                    }
                }
            ) { Text("Добавить") }
        }

        Spacer(Modifier.height(12.dp))

        when {
            loading -> Text("Загрузка...")
            steps.isEmpty() -> Text("Пока шагов нет")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(steps, key = { it.id }) { s ->
                    Card {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(s.title, style = MaterialTheme.typography.titleMedium)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Checkbox(
                                    checked = s.isDone,
                                    onCheckedChange = { checked ->
                                        scope.launch {
                                            try {
                                                repo.toggleStep(s.id, checked)
                                                load()
                                            } catch (e: Exception) {
                                                error = e.message
                                            }
                                        }
                                    }
                                )

                                OutlinedButton(
                                    onClick = {
                                        editStep = s
                                        editText = s.title
                                    }
                                ) { Text("Изм.") }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                repo.deleteStep(s.id)
                                                load()
                                            } catch (e: Exception) {
                                                error = e.message
                                            }
                                        }
                                    }
                                ) { Text("X") }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог редактирования
    if (editStep != null) {
        AlertDialog(
            onDismissRequest = { editStep = null },
            title = { Text("Редактировать шаг") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val s = editStep ?: return@TextButton
                    val t = editText.trim()
                    if (t.isEmpty()) return@TextButton
                    scope.launch {
                        try {
                            repo.renameStep(s.id, t)
                            editStep = null
                            load()
                        } catch (e: Exception) {
                            error = e.message
                        }
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { editStep = null }) { Text("Отмена") }
            }
        )
    }
}