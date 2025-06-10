package io.track.habit.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import io.track.habit.R
import io.track.habit.ui.composables.BottomNavBar
import io.track.habit.ui.navigation.SubNavRoute
import io.track.habit.ui.navigation.TopNavRoute
import io.track.habit.ui.navigation.isSelected
import io.track.habit.ui.navigation.navigateIfResumed
import io.track.habit.ui.screens.create.CreateScreen
import io.track.habit.ui.screens.habits.HabitsScreen
import io.track.habit.ui.screens.streaks.StreaksScreen
import io.track.habit.ui.theme.TrackAHabitTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackAHabitTheme {
                App()
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Composable
private fun App() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showFab =
        remember(currentDestination) {
            currentDestination?.isSelected(TopNavRoute.Habits) == true
        }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigateIfResumed(SubNavRoute.HabitsCreate)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add_a_habit),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = TopNavRoute.Habits,
        ) {
            composable<TopNavRoute.Habits> {
                HabitsScreen(
                    onViewLogs = {
                        navController.navigateIfResumed(
                            SubNavRoute.HabitsViewLogs(habitId = it.habitId),
                        )
                    },
                )
            }

            composable<SubNavRoute.HabitsCreate> {
                CreateScreen(onNavigateBack = navController::navigateUp)
            }

            composable<SubNavRoute.HabitsViewLogs> {
                val habitId = it.toRoute<SubNavRoute.HabitsViewLogs>()
                Greeting("Viewing Habit Logs for Habit ID: ${habitId.habitId}")
            }

            composable<TopNavRoute.Streaks> {
                StreaksScreen()
            }

            composable<TopNavRoute.Pomodoro> {
                Greeting("Pomodoro")
            }

            composable<TopNavRoute.Settings> {
                Greeting("Settings")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppPreview() {
    TrackAHabitTheme {
        App()
    }
}
