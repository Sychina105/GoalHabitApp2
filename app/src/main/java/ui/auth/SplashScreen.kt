package com.example.goalhabitapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.goalhabitapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    authRepo: AuthRepository,
    goLogin: () -> Unit,
    goTemplates: () -> Unit
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val ok = authRepo.isAuthorized()
            if (ok) goTemplates() else goLogin()
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Загрузка...")
    }
}
