package com.example.goalhabitapp.ui.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.goalhabitapp.data.remote.dto.PublicUserDto
import com.example.goalhabitapp.data.repository.FriendsRepository
import kotlinx.coroutines.launch

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
            } catch (e: retrofit2.HttpException) {
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Друзья", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("Назад") }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Поиск пользователя") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        Button(onClick = { load() }) { Text("Искать") }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        if (loading) {
            Text("Загрузка...")
            return@Column
        }


        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users) { u ->
                Card {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(u.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                when (u.status) {
                                    "FRIEND" -> "Друг"
                                    "OUTGOING" -> "Ожидание"
                                    "INCOMING" -> "Вам отправили запрос"
                                    else -> "Не в друзьях"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        when (u.status) {
                            "NONE" -> Button(onClick = {
                                scope.launch { repo.add(u.id); load() }
                            }) { Text("Добавить") }

                            "INCOMING" -> Button(onClick = {
                                scope.launch { repo.add(u.id); load() }
                            }) { Text("Принять") }

                            "FRIEND" -> OutlinedButton(onClick = {
                                onOpenProfile(u.id)
                            }) { Text("Профиль") }

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
