package com.example.goalhabitapp.ui.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.repository.FriendsRepository
import kotlinx.coroutines.launch

@Composable
fun FriendProfileScreen(
    friendId: Int,
    repo: FriendsRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var streak by remember { mutableStateOf(0) }
    var goals by remember { mutableStateOf(0) }
    var achievements by remember { mutableStateOf(emptyList<com.example.goalhabitapp.data.remote.dto.AchievementDto>()) }

    LaunchedEffect(friendId) {
        scope.launch {
            try {
                val p = repo.profile(friendId)
                name = p.user.name
                streak = p.currentHabitStreak
                goals = p.goalsCompleted
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
            Text("Профиль друга", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("Назад") }
        }

        Spacer(Modifier.height(16.dp))

        when {
            loading -> Text("Загрузка...")
            error != null -> Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
            else -> {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(name, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("Стрик: $streak дней")
                        Text("Завершено целей: $goals")
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Награды", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

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
                                Modifier.fillMaxWidth().padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(a.title)
                            }
                        }
                    }
                }
            }
        }
    }
}
