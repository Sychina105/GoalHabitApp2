package com.example.goalhabitapp.ui.profile
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.repository.ProfileRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    repo: ProfileRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit
)
 {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var streak by remember { mutableStateOf(0) }
    var goalsDone by remember { mutableStateOf(0) }
    var achievements by remember { mutableStateOf(emptyList<com.example.goalhabitapp.data.remote.dto.AchievementDto>()) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val p = repo.load()
                streak = p.currentHabitStreak
                goalsDone = p.goalsCompleted
                achievements = p.achievements
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
            TextButton(onClick = onBack) { Text("Назад") }
        }

        Spacer(Modifier.height(16.dp))

        if (loading) {
            Text("Загрузка...")
            return@Column
        }

        error?.let {
            Text("Ошибка: $it", color = MaterialTheme.colorScheme.error)
            return@Column
        }
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Выйти", color = MaterialTheme.colorScheme.onError)
        }


        // --- статистика ---
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Текущий стрик: $streak дней", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Завершено целей: $goalsDone")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Награды", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        // --- награды ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
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
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(a.title)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))



    }
}
