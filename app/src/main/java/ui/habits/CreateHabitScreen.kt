package com.example.goalhabitapp.ui.habits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.HabitCreateRequest
import com.example.goalhabitapp.data.repository.HabitsRepository
import kotlinx.coroutines.launch

enum class FreqMode {
    DAILY,          // каждый день
    EVERY_N_DAYS,   // каждые N дней
    N_PER_WEEK,     // N раз в неделю
    N_PER_MONTH,    // N раз в месяц
    N_PER_N_DAYS    // N раз в N дней
}

@Composable
fun CreateHabitScreen(
    repo: HabitsRepository,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }

    // выбранная частота, которую будем отправлять на сервер
    var periodDays by remember { mutableStateOf(1) }
    var timesPerPeriod by remember { mutableStateOf(1) }

    var showFreqDialog by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun freqLabel(): String = when {
        periodDays == 1 && timesPerPeriod == 1 -> "Каждый день"
        timesPerPeriod == 1 -> "Каждые $periodDays дней"
        periodDays == 7 -> "$timesPerPeriod раз в неделю"
        periodDays == 30 -> "$timesPerPeriod раз в месяц"
        else -> "$timesPerPeriod раз в $periodDays дней"
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Новая привычка", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Что делаем?") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = freqLabel(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Частота") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showFreqDialog = true }
        )

        error?.let { Text("Ошибка: $it", color = MaterialTheme.colorScheme.error) }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Назад") }

            Button(
                onClick = {
                    loading = true
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

                            repo.create(
                                HabitCreateRequest(
                                    title = t,
                                    periodDays = periodDays,
                                    timesPerPeriod = timesPerPeriod
                                )
                            )
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
            ) { Text(if (loading) "..." else "Сохранить") }
        }
    }

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

@Composable
     fun FrequencyDialog(
    initialPeriodDays: Int,
    initialTimesPerPeriod: Int,
    onDismiss: () -> Unit,
    onConfirm: (periodDays: Int, timesPerPeriod: Int) -> Unit
) {
    var mode by remember { mutableStateOf(FreqMode.DAILY) }

    var everyNDays by remember { mutableStateOf("3") }
    var nPerWeek by remember { mutableStateOf("3") }
    var nPerMonth by remember { mutableStateOf("10") }
    var timesN by remember { mutableStateOf("3") }     // N раз
    var periodN by remember { mutableStateOf("14") }   // в N дней

    // Попробуем предзаполнить режим из текущих значений
    LaunchedEffect(Unit) {
        when {
            initialPeriodDays == 1 && initialTimesPerPeriod == 1 -> mode = FreqMode.DAILY
            initialTimesPerPeriod == 1 -> { mode = FreqMode.EVERY_N_DAYS; everyNDays = initialPeriodDays.toString() }
            initialPeriodDays == 7 -> { mode = FreqMode.N_PER_WEEK; nPerWeek = initialTimesPerPeriod.toString() }
            initialPeriodDays == 30 -> { mode = FreqMode.N_PER_MONTH; nPerMonth = initialTimesPerPeriod.toString() }
            else -> { mode = FreqMode.N_PER_N_DAYS; timesN = initialTimesPerPeriod.toString(); periodN = initialPeriodDays.toString() }
        }
    }

    fun onlyDigits(s: String) = s.filter { it.isDigit() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val (pd, tp) = when (mode) {
                    FreqMode.DAILY -> 1 to 1
                    FreqMode.EVERY_N_DAYS -> (everyNDays.toIntOrNull() ?: 0) to 1
                    FreqMode.N_PER_WEEK -> 7 to (nPerWeek.toIntOrNull() ?: 0)
                    FreqMode.N_PER_MONTH -> 30 to (nPerMonth.toIntOrNull() ?: 0)
                    FreqMode.N_PER_N_DAYS -> (periodN.toIntOrNull() ?: 0) to (timesN.toIntOrNull() ?: 0)
                }
                // простая валидация
                if (pd > 0 && tp > 0) onConfirm(pd, tp)
            }) { Text("ОК") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        title = { Text("Выбор частоты") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = mode == FreqMode.DAILY, onClick = { mode = FreqMode.DAILY })
                    Text("Каждый день")
                }

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = mode == FreqMode.EVERY_N_DAYS, onClick = { mode = FreqMode.EVERY_N_DAYS })
                    Text("Каждые")
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = everyNDays,
                        onValueChange = { everyNDays = onlyDigits(it) },
                        modifier = Modifier.width(90.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("дней")
                }

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = mode == FreqMode.N_PER_WEEK, onClick = { mode = FreqMode.N_PER_WEEK })
                    OutlinedTextField(
                        value = nPerWeek,
                        onValueChange = { nPerWeek = onlyDigits(it) },
                        modifier = Modifier.width(90.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("раз в неделю")
                }

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = mode == FreqMode.N_PER_MONTH, onClick = { mode = FreqMode.N_PER_MONTH })
                    OutlinedTextField(
                        value = nPerMonth,
                        onValueChange = { nPerMonth = onlyDigits(it) },
                        modifier = Modifier.width(90.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("раз в месяц")
                }

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = mode == FreqMode.N_PER_N_DAYS, onClick = { mode = FreqMode.N_PER_N_DAYS })
                    OutlinedTextField(
                        value = timesN,
                        onValueChange = { timesN = onlyDigits(it) },
                        modifier = Modifier.width(90.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("раз в")
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = periodN,
                        onValueChange = { periodN = onlyDigits(it) },
                        modifier = Modifier.width(90.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("дней")
                }
            }
        }
    )
}
