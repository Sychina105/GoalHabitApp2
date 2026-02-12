package com.example.goalhabitapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onGoTemplates: () -> Unit,
    onGoHabits: () -> Unit,
    onGoGoals: () -> Unit,
    onGoProfile: () -> Unit,
    onGoFriends: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Главная",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onGoGoals,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Мои цели")
        }

        Button(
            onClick = onGoHabits,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Мои привычки")
        }

        OutlinedButton(
            onClick = onGoTemplates,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Шаблоны целей")
        }

        Button(
            onClick = onGoProfile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Профиль")
        }

        Button(
            onClick = onGoFriends,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Друзья")
        }
    }
}
