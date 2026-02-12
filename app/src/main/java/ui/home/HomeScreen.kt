package com.example.goalhabitapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    onGoProfile: () -> Unit

) {
    Button(onClick = onGoProfile) { Text("Профиль") }

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
            modifier = Modifier.fillMaxSize(fraction = 0.2f)
        ) {
            Text("Мои цели")
        }

        Button(
            onClick = onGoHabits,
            modifier = Modifier.fillMaxSize(fraction = 0.2f)
        ) {
            Text("Мои привычки")
        }

        OutlinedButton(
            onClick = onGoTemplates,
            modifier = Modifier.fillMaxSize(fraction = 0.2f)
        ) {
            Text("Шаблоны целей")
        }
    }
}
