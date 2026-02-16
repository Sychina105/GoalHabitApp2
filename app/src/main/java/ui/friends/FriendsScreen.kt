package com.example.goalhabitapp.ui.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.PublicUserDto
import com.example.goalhabitapp.data.repository.FriendsRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun FriendsScreen(
    repo: FriendsRepository,
    onOpenProfile: (Int) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<PublicUserDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                users = repo.search(query.ifBlank { null })
            } catch (e: HttpException) {
                error = "Ошибка сервера: ${e.code()}"
            } catch (e: Exception) {
                error = "Ошибка: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun doAction(block: suspend () -> Unit) {
        loading = true
        error = null
        scope.launch {
            try {
                block()
                load()
            } catch (e: HttpException) {
                error = "Ошибка сервера: ${e.code()}"
            } catch (e: Exception) {
                error = "Ошибка: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Друзья", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("Назад") }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Поиск по имени/почте") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { load() }) { Text("Искать") }
            OutlinedButton(onClick = {
                query = ""
                load()
            }) { Text("Сброс") }
        }

        Spacer(Modifier.height(12.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        if (loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users) { u ->
                FriendRow(
                    u = u,
                    onOpenProfile = onOpenProfile,
                    onAdd = { doAction { repo.add(u.id) } },
                    onAccept = { doAction { repo.add(u.id) } },       // ✅ принять = add()
                    onDecline = { doAction { repo.remove(u.id) } },   // ✅ отклонить = remove()
                    onCancel = { doAction { repo.remove(u.id) } },    // ✅ отмена исходящего = remove()
                    onRemove = { doAction { repo.remove(u.id) } }     // ✅ удалить друга = remove()
                )
            }
        }
    }
}

@Composable
private fun FriendRow(
    u: PublicUserDto,
    onOpenProfile: (Int) -> Unit,
    onAdd: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onCancel: () -> Unit,
    onRemove: () -> Unit
) {
    Card {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(u.name, style = MaterialTheme.typography.titleMedium)

                    val statusText = when (u.status) {
                        "FRIEND" -> "Друг"
                        "OUTGOING" -> "Запрос отправлен"
                        "INCOMING" -> "Входящий запрос"
                        else -> "Не в друзьях"
                    }
                    Text(statusText, style = MaterialTheme.typography.bodySmall)
                }

                // ✅ Индикаторы (если есть)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    u.currentHabitStreak?.let { s ->
                        AssistChip(
                            onClick = {},
                            label = { Text("🔥 $s") }
                        )
                    }
                    u.achievementsEarned?.let { a ->
                        AssistChip(
                            onClick = {},
                            label = { Text("🏆 $a") }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ✅ Кнопки действий по статусу
            when (u.status) {
                "NONE" -> {
                    Button(onClick = onAdd) { Text("Добавить") }
                }

                "OUTGOING" -> {
                    OutlinedButton(onClick = onCancel) { Text("Отменить") }
                }

                "INCOMING" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onAccept) { Text("Принять") }
                        OutlinedButton(onClick = onDecline) { Text("Отклонить") }
                    }
                }

                "FRIEND" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onOpenProfile(u.id) }) { Text("Профиль") }
                        OutlinedButton(onClick = onRemove) { Text("Удалить") }
                    }
                }
            }
        }
    }
}
