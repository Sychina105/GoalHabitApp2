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
        import com.example.goalhabitapp.data.repository.FriendsRepository
        import com.example.goalhabitapp.ui.friends.FriendsScreen
        import com.example.goalhabitapp.ui.friends.FriendProfileScreen
        import kotlinx.coroutines.launch
        import android.Manifest
        import android.os.Build
        import androidx.activity.compose.rememberLauncherForActivityResult
        import androidx.activity.result.contract.ActivityResultContracts
        import androidx.compose.runtime.LaunchedEffect
        import androidx.compose.ui.platform.LocalContext
        import com.example.goalhabitapp.notifications.ReminderScheduler

        class MainActivity : ComponentActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

                setContent {
                    MaterialTheme {
                        Surface {
                            val nav = rememberNavController()
                            val scope = androidx.compose.runtime.rememberCoroutineScope()
                            val context = LocalContext.current
                            val notifPermLauncher = rememberLauncherForActivityResult(
                                ActivityResultContracts.RequestPermission()
                            ) { granted ->
                                // даже если не дали — приложение работает, просто без уведомлений
                                if (granted) {
                                    ReminderScheduler.scheduleDaily(context, hour = 22, minute = 0)
                                }
                            }

                            LaunchedEffect(Unit) {
                                if (Build.VERSION.SDK_INT >= 33) {
                                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    ReminderScheduler.scheduleDaily(context, hour = 20, minute = 0)
                                }
                            }
                            val tokenStore = remember { TokenStore(this) }
                            val api = remember { Network.api("http://10.0.2.2:8000/", tokenStore) }

                            val profileRepo = remember { ProfileRepository(api) }
                            val authRepo = remember { AuthRepository(api, tokenStore) }
                            val templatesRepo = remember { TemplatesRepository(api) }
                            val habitsRepo = remember { HabitsRepository(api) }
                            val goalsRepo = remember { GoalsRepository(api) }
                            val friendsRepo = remember { FriendsRepository(api) }


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
                                        onGoProfile = { nav.navigate(Routes.Profile) },
                                        onGoFriends = { nav.navigate(Routes.Friends) }
                                    )


                                }

                                // ✅ HABITS
                                composable(Routes.Habits) {
                                    com.example.goalhabitapp.ui.habits.HabitsScreen(
                                        repo = habitsRepo,
                                        onCreate = { nav.navigate(Routes.CreateHabit) },
                                        onEdit = { id -> nav.navigate(Routes.editHabit(id)) },
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


                                composable(Routes.Friends) {
                                    FriendsScreen(
                                        repo = friendsRepo,
                                        onOpenProfile = { id ->
                                            nav.navigate(Routes.friendProfile(id))
                                        },
                                        onBack = { nav.popBackStack() }
                                    )
                                }

                                composable(Routes.FriendProfile) { backStack ->
                                    val id = backStack.arguments?.getString("id")!!.toInt()
                                    FriendProfileScreen(
                                        friendId = id,
                                        repo = friendsRepo,
                                        onBack = { nav.popBackStack() }
                                    )
                                }
                                composable(Routes.EditHabit) { backStack ->
                                    val id = backStack.arguments?.getString("id")!!.toLong()
                                    com.example.goalhabitapp.ui.habits.EditHabitScreen(
                                        habitId = id,
                                        repo = habitsRepo,
                                        onDone = { nav.popBackStack() },
                                        onBack = { nav.popBackStack() }
                                    )
                                }


                                // ✅ GOALS
                                composable(Routes.Goals) {
                                    com.example.goalhabitapp.ui.goals.GoalsScreen(
                                        repo = goalsRepo,
                                        onCreate = { nav.navigate(Routes.CreateGoal) },
                                        onEdit = { id -> nav.navigate(Routes.editGoal(id)) },
                                        onBack = { nav.popBackStack() },
                                        onSteps = { id -> nav.navigate(Routes.goalSteps(id)) } // ✅
                                    )
                                }
                                composable(Routes.CreateGoal) {
                                    com.example.goalhabitapp.ui.goals.CreateGoalScreen(
                                        repo = goalsRepo,
                                        onDone = { nav.popBackStack() },
                                        onBack = { nav.popBackStack() }
                                    )
                                }

                                composable(Routes.GoalSteps) { backStack ->
                                    val id = backStack.arguments?.getString("id")!!.toLong()
                                    com.example.goalhabitapp.ui.goals.GoalStepsScreen(
                                        goalId = id,
                                        repo = goalsRepo,
                                        onBack = { nav.popBackStack() }
                                    )
                                }
                                composable(Routes.EditGoal) { backStack ->
                                    val id = backStack.arguments?.getString("id")!!.toLong()
                                    com.example.goalhabitapp.ui.goals.EditGoalScreen(
                                        goalId = id,
                                        repo = goalsRepo,
                                        onDone = { nav.popBackStack() },
                                        onBack = { nav.popBackStack() }
                                    )

                                }

                                // ✅ TEMPLATES
                                composable(Routes.Templates) {
                                    TemplatesScreen(
                                        templatesRepo = templatesRepo,
                                        goalsRepo = goalsRepo,
                                        onGoGoals = { nav.navigate(Routes.Goals) },
                                        onLogout = {
                                            nav.navigate(Routes.Login) {
                                                popUpTo(Routes.Home) { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable(Routes.Profile) {
                                    ProfileScreen(
                                        repo = profileRepo,
                                        onBack = { nav.popBackStack() },
                                        onLogout = {
                                            scope.launch {
                                                tokenStore.clear()
                                                nav.navigate(Routes.Login) {
                                                    popUpTo(Routes.Home) { inclusive = true }
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
