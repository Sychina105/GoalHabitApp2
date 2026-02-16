package com.example.goalhabitapp.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.GoalCreateRequest
import com.example.goalhabitapp.data.remote.dto.GoalTemplateDto
import com.example.goalhabitapp.data.repository.GoalsRepository
import com.example.goalhabitapp.data.repository.TemplatesRepository
import kotlinx.coroutines.launch

@Composable
fun TemplatesScreen(
    templatesRepo: TemplatesRepository,
    goalsRepo: GoalsRepository,
    onGoGoals: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf<List<GoalTemplateDto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var info by remember { mutableStateOf<String?>(null) }

    // чтобы не жать по 20 раз
    var creatingTemplateId by remember { mutableStateOf<Long?>(null) }

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                items = templatesRepo.getTemplates()
            } catch (e: Exception) {
                error = "Ошибка загрузки: ${e.message ?: "неизвестно"}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Шаблоны целей", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onGoGoals) { Text("Цели") }
                TextButton(onClick = onLogout) { Text("Выйти") }
            }
        }

        Spacer(Modifier.height(12.dp))

        info?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }

        when {
            loading -> Text("Загрузка...")
            error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items) { t ->
                        val isCreating = creatingTemplateId == t.id

                        Card {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(t.title, style = MaterialTheme.typography.titleMedium)
                                Text(t.description)

                                Spacer(Modifier.height(6.dp))
                                Text("Категория: ${t.category}")
                                Text("Рекомендуемо: ${t.suggestedTarget ?: "-"} ${t.suggestedUnit ?: ""}".trim())

                                Spacer(Modifier.height(10.dp))

                                Button(
                                    enabled = !isCreating,
                                    onClick = {
                                        scope.launch {
                                            creatingTemplateId = t.id
                                            error = null
                                            info = null
                                            try {
                                                // Логика:
                                                // если есть suggestedTarget -> QUANT
                                                // иначе -> STEPS (без target/unit)
                                                val isQuant = t.suggestedTarget != null

                                                val req = GoalCreateRequest(
                                                    title = t.title,
                                                    description = t.description.ifBlank { null },
                                                    goalType = if (isQuant) "QUANT" else "STEPS",
                                                    targetValue = if (isQuant) t.suggestedTarget else null,
                                                    unit = if (isQuant) t.suggestedUnit else null,
                                                    deadline = null,
                                                    priority = 3
                                                )

                                                goalsRepo.create(req)
                                                info = "Цель создана ✅"
                                                // можно сразу перейти в "Цели"
                                                onGoGoals()
                                            } catch (e: Exception) {
                                                error = "Ошибка создания: ${e.message ?: "неизвестно"}"
                                            } finally {
                                                creatingTemplateId = null
                                            }
                                        }
                                    }
                                ) {
                                    Text(if (isCreating) "Создаю..." else "Создать по шаблону")
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { load() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Обновить шаблоны")
                }
            }
        }
    }
}
