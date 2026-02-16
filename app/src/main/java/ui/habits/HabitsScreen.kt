package com.example.goalhabitapp.ui.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.HabitDto
import com.example.goalhabitapp.data.repository.HabitsRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun HabitsScreen(
    repo: HabitsRepository,
    onCreate: () -> Unit,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<HabitDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    val today = remember { LocalDate.now().toString() }
    var checkedTodayIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    fun freqLabel(h: HabitDto): String = when {
        h.periodDays == 1 && h.timesPerPeriod == 1 -> "Каждый день"
        h.timesPerPeriod == 1 -> "Каждые ${h.periodDays} дней"
        h.periodDays == 7 -> "${h.timesPerPeriod} раз в неделю"
        h.periodDays == 30 -> "${h.timesPerPeriod} раз в месяц"
        else -> "${h.timesPerPeriod} раз в ${h.periodDays} дней"
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
            Text("Привычки", style = MaterialTheme.typography.headlineSmall)
            Row {
                TextButton(onClick = onBack) { Text("Назад") }
                TextButton(onClick = onCreate) { Text("+") }
            }
        }

        Spacer(Modifier.height(12.dp))

        info?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }

        when {
            loading -> Text("Загрузка...")
            error != null -> Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items) { h ->
                    val checkedToday = checkedTodayIds.contains(h.id)

                    Card {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(h.title, style = MaterialTheme.typography.titleMedium)
                            Text("Частота: ${freqLabel(h)}")
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                repo.checkIn(h.id, today)
                                                checkedTodayIds = checkedTodayIds + h.id
                                                info = "Отмечено за сегодня ✅"
                                            } catch (e: Exception) {
                                                error = "Check-in: ${e.message}"
                                            }
                                        }
                                    },
                                    enabled = !checkedToday,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (checkedToday) "Отмечено ✅" else "Отметить сегодня")
                                }

                                OutlinedButton(
                                    onClick = { onEdit(h.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Редактировать")
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            repo.delete(h.id)
                                            checkedTodayIds = checkedTodayIds - h.id
                                            load()
                                        } catch (e: Exception) {
                                            error = e.message
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Удалить")
                            }

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
