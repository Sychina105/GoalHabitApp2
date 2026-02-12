package com.example.goalhabitapp.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.GoalTemplateDto
import com.example.goalhabitapp.data.repository.TemplatesRepository
import kotlinx.coroutines.launch

@Composable
fun TemplatesScreen(
    templatesRepo: TemplatesRepository,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<GoalTemplateDto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
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

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Шаблоны целей", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onLogout) { Text("Выйти") }
        }

        Spacer(Modifier.height(12.dp))

        when {
            loading -> Text("Загрузка...")
            error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items) { t ->
                        Card {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(t.title, style = MaterialTheme.typography.titleMedium)
                                Text(t.description)
                                Spacer(Modifier.height(6.dp))
                                Text("Категория: ${t.category}")
                                Text("Рекомендуемо: ${t.suggestedTarget ?: "-"} ${t.suggestedUnit ?: ""}")
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = { /* потом: создать цель по шаблону */ }) {
                                    Text("Создать по шаблону")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
