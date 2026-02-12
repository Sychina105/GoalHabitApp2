package com.example.goalhabitapp.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.GoalDto
import com.example.goalhabitapp.data.repository.GoalsRepository
import kotlinx.coroutines.launch

@Composable
fun GoalsScreen(
    repo: GoalsRepository,
    onCreate: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<GoalDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

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

        Spacer(Modifier.height(12.dp))

        when {
            loading -> Text("Загрузка...")
            error != null -> Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items) { g ->
                    Card {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(g.title, style = MaterialTheme.typography.titleMedium)
                            Text("Тип: ${g.goalType} | Статус: ${g.status} | Приоритет: ${g.priority}")
                            Text("Прогресс: ${g.progressValue}" + (g.targetValue?.let { "/$it" } ?: "") + (g.unit?.let { " $it" } ?: ""))
                            g.deadline?.let { Text("Дедлайн: $it") }
                            g.description?.takeIf { it.isNotBlank() }?.let { Text(it) }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { load() }, modifier = Modifier.fillMaxWidth()) { Text("Обновить") }

    }
}
