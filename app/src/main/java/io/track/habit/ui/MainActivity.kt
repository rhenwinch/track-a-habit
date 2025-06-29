package io.track.habit.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.track.habit.ui.composables.BottomNavBar
import io.track.habit.ui.navigation.NavRoute
import io.track.habit.ui.navigation.SubNavRoute
import io.track.habit.ui.navigation.TopNavRoute
import io.track.habit.ui.navigation.isSelected
import io.track.habit.ui.navigation.navigateIfResumed
import io.track.habit.ui.screens.create.CREATE_HABIT_KEY
import io.track.habit.ui.screens.create.CreateScreen
import io.track.habit.ui.screens.habits.HabitsScreen
import io.track.habit.ui.screens.logs.LogsScreen
import io.track.habit.ui.screens.onboarding.OnboardingScreen
import io.track.habit.ui.screens.settings.SettingsScreen
import io.track.habit.ui.screens.streaks.StreaksScreen
import io.track.habit.ui.theme.TrackAHabitTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class MainActivity : FragmentActivity() { // MainActivity is a FragmentActivity to support biometrics
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(viewModel.googleDriveService) {
            initialize(
                activity = this@MainActivity,
                launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                    handleAuthorizationResult(result.resultCode, result.data)
                },
            )
        }

        enableEdgeToEdge()
        setContent {
            TrackAHabitTheme {
                App(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun App(viewModel: MainViewModel) {
    val isFirstRun by viewModel.isFirstRun.collectAsStateWithLifecycle()

    AppContent(
        startDestination =
            if (isFirstRun) {
                TopNavRoute.Onboarding
            } else {
                TopNavRoute.Habits
            },
    )
}

@Composable
private fun AppContent(startDestination: NavRoute) {
    val navController = rememberNavController()
    val gson = remember { Gson() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar =
        remember(currentDestination) {
            currentDestination?.isSelected(SubNavRoute.HabitsCreate) == false &&
                !currentDestination.isSelected(SubNavRoute.HabitsViewLogs(1)) &&
                startDestination !is TopNavRoute.Onboarding
        }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it / 2 },
            ) {
                BottomNavBar(navController = navController)
            }
        },
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = startDestination,
        ) {
            composable<TopNavRoute.Onboarding> {
                // Navigate to the onboarding screen only if the start destination is Habits
                // This is to prevent navigating to Onboarding when the user is already onboarded.
                LaunchedEffect(startDestination) {
                    if (startDestination is TopNavRoute.Habits) {
                        navController.navigateIfResumed(TopNavRoute.Habits)
                    }
                }

                OnboardingScreen()
            }

            composable<TopNavRoute.Habits> {
                HabitsScreen(
                    savedStateHandle = navBackStackEntry?.savedStateHandle,
                    onViewLogs = {
                        navController.navigateIfResumed(
                            SubNavRoute.HabitsViewLogs(habitId = it.habitId),
                        )
                    },
                    onAddHabit = {
                        navController.navigateIfResumed(SubNavRoute.HabitsCreate)
                    },
                )
            }

            composable<SubNavRoute.HabitsCreate> {
                CreateScreen(
                    onNavigateBack = navController::navigateUp,
                    onNavigateWithResult = {
                        val habitJson = gson.toJson(it)

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(CREATE_HABIT_KEY, habitJson)

                        navController.navigateUp()
                    },
                )
            }

            composable<SubNavRoute.HabitsViewLogs> {
                LogsScreen(onNavigateBack = navController::navigateUp)
            }

            composable<TopNavRoute.Streaks> {
                StreaksScreen()
            }

            composable<TopNavRoute.Settings> {
                SettingsScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppOnHabitsPreview() {
    TrackAHabitTheme {
        AppContent(
            startDestination = TopNavRoute.Habits,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppOnOnboardingPreview() {
    TrackAHabitTheme {
        AppContent(
            startDestination = TopNavRoute.Onboarding,
        )
    }
}
