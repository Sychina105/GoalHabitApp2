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
        import com.example.goalhabitapp.data.repository.HabitsRepository
        import com.example.goalhabitapp.data.repository.GoalsRepository
        import com.example.goalhabitapp.ui.profile.ProfileScreen
        import com.example.goalhabitapp.data.repository.ProfileRepository

        class MainActivity : ComponentActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

                setContent {
                    MaterialTheme {
                        Surface {
                            val nav = rememberNavController()

                            val tokenStore = remember { TokenStore(this) }
                            val api = remember { Network.api("http:///10.0.2.2:8000/", tokenStore) }
                            val profileRepo = remember { ProfileRepository(api) }
                            val authRepo = remember { AuthRepository(api, tokenStore) }
                            val templatesRepo = remember { TemplatesRepository(api) }
                            val habitsRepo = remember { HabitsRepository(api) }
                            val goalsRepo = remember { GoalsRepository(api) }

                            NavHost(navController = nav, startDestination = Routes.Splash) {

                                composable(Routes.Splash) {
                                    SplashScreen(
                                        authRepo = authRepo,
                                        goLogin = {
                                            nav.navigate(Routes.Login) {
                                                popUpTo(Routes.Splash) {
                                                    inclusive = true
                                                }
                                            }
                                        },
                                        goTemplates = {
                                            nav.navigate(Routes.Home) {
                                                popUpTo(Routes.Splash) {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    )
                                }

                                composable(Routes.Login) {
                                    LoginScreen(
                                        authRepo = authRepo,
                                        onGoRegister = { nav.navigate(Routes.Register) },
                                        onSuccess = {
                                            nav.navigate(Routes.Home) {
                                                popUpTo(Routes.Login) {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    )
                                }

                                composable(Routes.Register) {
                                    RegisterScreen(
                                        authRepo = authRepo,
                                        onBack = { nav.popBackStack() },
                                        onSuccess = {
                                            nav.navigate(Routes.Home) {
                                                popUpTo(Routes.Register) {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    )
                                }

                                // ✅ HOME
                                composable(Routes.Home) {
                                    com.example.goalhabitapp.ui.home.HomeScreen(
                                        onGoTemplates = { nav.navigate(Routes.Templates) },
                                        onGoHabits = { nav.navigate(Routes.Habits) },
                                        onGoGoals = { nav.navigate(Routes.Goals) },
                                        onGoProfile = { nav.navigate(Routes.Profile) }
                                    )

                                }

                                // ✅ HABITS
                                composable(Routes.Habits) {
                                    com.example.goalhabitapp.ui.habits.HabitsScreen(
                                        repo = habitsRepo,
                                        onCreate = { nav.navigate(Routes.CreateHabit) },
                                        onBack = { nav.popBackStack() }
                                    )
                                }

                                composable(Routes.CreateHabit) {
                                    com.example.goalhabitapp.ui.habits.CreateHabitScreen(
                                        repo = habitsRepo,
                                        onDone = { nav.popBackStack() },
                                        onBack = { nav.popBackStack() }
                                    )
                                }
                                composable(Routes.Profile) {
                                    ProfileScreen(
                                        repo = profileRepo,
                                        onBack = { nav.popBackStack() }
                                    )
                                }


                                // ✅ GOALS
                                composable(Routes.Goals) {
                                    com.example.goalhabitapp.ui.goals.GoalsScreen(
                                        repo = goalsRepo,
                                        onCreate = { nav.navigate(Routes.CreateGoal) },
                                        onBack = { nav.popBackStack() }
                                    )
                                }

                                composable(Routes.CreateGoal) {
                                    com.example.goalhabitapp.ui.goals.CreateGoalScreen(
                                        repo = goalsRepo,
                                        onDone = { nav.popBackStack() },
                                        onBack = { nav.popBackStack() }
                                    )
                                }

                                // ✅ TEMPLATES
                                composable(Routes.Templates) {
                                    TemplatesScreen(
                                        templatesRepo = templatesRepo,
                                        onLogout = {
                                            // (опционально) тут можно вызвать authRepo.logout() в корутине
                                            nav.navigate(Routes.Login) {
                                                popUpTo(Routes.Home) {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
