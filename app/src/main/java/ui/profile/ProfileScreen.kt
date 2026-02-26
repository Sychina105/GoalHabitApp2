package com.example.goalhabitapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.AchievementDto
import com.example.goalhabitapp.data.remote.dto.GoalDto
import com.example.goalhabitapp.data.repository.ProfileRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    repo: ProfileRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var points by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var goalsDone by remember { mutableStateOf(0) }
    var achievements by remember { mutableStateOf<List<AchievementDto>>(emptyList()) }
    var goals by remember { mutableStateOf<List<GoalDto>>(emptyList()) } // ✅ цели для профиля

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val p = repo.load()
                streak = p.currentHabitStreak
                goalsDone = p.goalsCompleted
                achievements = p.achievements
                goals = p.goals // ✅ тут
                points = p.points
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Профиль", style = MaterialTheme.typography.headlineSmall)
            Row {
                TextButton(onClick = onBack) { Text("Назад") }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (loading) {
            Text("Загрузка...")
            return@Column
        }

        error?.let {
            Text("Ошибка: $it", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Выйти", color = MaterialTheme.colorScheme.onError)
        }

        Spacer(Modifier.height(12.dp))

        // --- статистика ---
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Текущий стрик: $streak дней", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Завершено целей: $goalsDone")
                Text("Очки: ${points}", style = MaterialTheme.typography.titleMedium)

            }
        }

        Spacer(Modifier.height(16.dp))

        // --- цели в профиле ---
        Text("Цели в профиле", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (goals.isEmpty()) {
            Text("Нет выбранных целей")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(goals) { g ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(g.title, style = MaterialTheme.typography.titleMedium)
                            Text("Тип: ${g.goalType} | Статус: ${g.status}")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- награды ---
        Text("Награды", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().height(320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(achievements) { a ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (a.earned_at != null)
                            MaterialTheme.colorScheme.primaryContainer
                        else Color.LightGray
                    )
                )

                {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(a.title)
                    }
                }

            }
        }
    }
}