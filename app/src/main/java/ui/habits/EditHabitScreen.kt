package com.example.goalhabitapp.ui.habits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.HabitUpdateRequest
import com.example.goalhabitapp.data.repository.HabitsRepository
import kotlinx.coroutines.launch

@Composable
fun EditHabitScreen(
    habitId: Long,
    repo: HabitsRepository,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var title by remember { mutableStateOf("") }

    // ✅ как в CreateHabitScreen
    var periodDays by remember { mutableStateOf(1) }
    var timesPerPeriod by remember { mutableStateOf(1) }
    var showFreqDialog by remember { mutableStateOf(false) }

    fun freqLabel(): String = when {
        periodDays == 1 && timesPerPeriod == 1 -> "Каждый день"
        timesPerPeriod == 1 -> "Каждые $periodDays дней"
        periodDays == 7 -> "$timesPerPeriod раз в неделю"
        periodDays == 30 -> "$timesPerPeriod раз в месяц"
        else -> "$timesPerPeriod раз в $periodDays дней"
    }

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                val h = repo.list().firstOrNull { it.id == habitId }
                if (h == null) {
                    error = "Привычка не найдена"
                } else {
                    title = h.title
                    periodDays = h.periodDays
                    timesPerPeriod = h.timesPerPeriod
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(habitId) { load() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Редактировать привычку", style = MaterialTheme.typography.headlineSmall)
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
            label = { Text("Что делаем?") },
            modifier = Modifier.fillMaxWidth()
        )

        // ✅ Частота как в CreateHabitScreen
        OutlinedTextField(
            value = freqLabel(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Частота") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showFreqDialog = true }
        )

        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Отмена") }

            Button(
                onClick = {
                    saving = true
                    error = null

                    scope.launch {
                        try {
                            val t = title.trim()
                            if (t.isEmpty()) {
                                error = "Введите текст привычки"
                                return@launch
                            }
                            if (periodDays <= 0 || timesPerPeriod <= 0) {
                                error = "Частота задана неверно"
                                return@launch
                            }

                            repo.update(
                                habitId,
                                HabitUpdateRequest(
                                    title = t,
                                    periodDays = periodDays,
                                    timesPerPeriod = timesPerPeriod
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

    // ✅ Диалог частоты — должен быть доступен (см. ниже)
    if (showFreqDialog) {
        FrequencyDialog(
            initialPeriodDays = periodDays,
            initialTimesPerPeriod = timesPerPeriod,
            onDismiss = { showFreqDialog = false },
            onConfirm = { pd, tp ->
                periodDays = pd
                timesPerPeriod = tp
                showFreqDialog = false
            }
        )
    }
}


