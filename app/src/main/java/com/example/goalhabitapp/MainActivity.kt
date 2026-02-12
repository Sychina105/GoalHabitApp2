package com.example.goalhabitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.goalhabitapp.data.local.TokenStore
import com.example.goalhabitapp.data.remote.Network
import com.example.goalhabitapp.data.repository.AuthRepository
import com.example.goalhabitapp.data.repository.TemplatesRepository
import com.example.goalhabitapp.navigation.Routes
import com.example.goalhabitapp.ui.auth.LoginScreen
import com.example.goalhabitapp.ui.auth.RegisterScreen
import com.example.goalhabitapp.ui.auth.SplashScreen
import com.example.goalhabitapp.ui.templates.TemplatesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    val nav = rememberNavController()

                    // важный baseUrl:
                    // Эмулятор Android: 10.0.2.2
                    val tokenStore = remember { TokenStore(this) }
                    val api = remember { Network.api("http://10.0.2.2:8000/", tokenStore) }

                    val authRepo = remember { AuthRepository(api, tokenStore) }
                    val templatesRepo = remember { TemplatesRepository(api) }

                    NavHost(navController = nav, startDestination = Routes.Splash) {

                        composable(Routes.Splash) {
                            SplashScreen(
                                authRepo = authRepo,
                                goLogin = { nav.navigate(Routes.Login) { popUpTo(Routes.Splash) { inclusive = true } } },
                                goTemplates = { nav.navigate(Routes.Templates) { popUpTo(Routes.Splash) { inclusive = true } } }
                            )
                        }

                        composable(Routes.Login) {
                            LoginScreen(
                                authRepo = authRepo,
                                onGoRegister = { nav.navigate(Routes.Register) },
                                onSuccess = { nav.navigate(Routes.Templates) { popUpTo(Routes.Login) { inclusive = true } } }
                            )
                        }

                        composable(Routes.Register) {
                            RegisterScreen(
                                authRepo = authRepo,
                                onBack = { nav.popBackStack() },
                                onSuccess = { nav.navigate(Routes.Templates) { popUpTo(Routes.Register) { inclusive = true } } }
                            )
                        }

                        composable(Routes.Templates) {
                            TemplatesScreen(templatesRepo = templatesRepo, onLogout = {
                                // на MVP просто возвращаем в login
                                nav.navigate(Routes.Login) { popUpTo(Routes.Templates) { inclusive = true } }
                            })
                        }
                    }
                }
            }
        }
    }
}
