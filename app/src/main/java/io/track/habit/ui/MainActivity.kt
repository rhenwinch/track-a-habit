package io.track.habit.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import io.track.habit.R
import io.track.habit.ui.composables.BottomNavBar
import io.track.habit.ui.navigation.NavRoute
import io.track.habit.ui.navigation.SubNavRoute
import io.track.habit.ui.navigation.TopLevelBackStack
import io.track.habit.ui.screens.create.CreateScreen
import io.track.habit.ui.screens.habits.HabitsScreen
import io.track.habit.ui.theme.TrackAHabitTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()

            TrackAHabitTheme {
                App(topLevelBackStack = viewModel.backStack)
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
private fun App(topLevelBackStack: TopLevelBackStack<NavKey>) {
    val currentRoute by remember {
        derivedStateOf { topLevelBackStack.backStack.lastOrNull() }
    }

    val showFab by remember {
        derivedStateOf {
            when (currentRoute) {
                is NavRoute.Companion.Habits -> true
                else -> false
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(backStack = topLevelBackStack)
        },
        floatingActionButton = {
            AnimatedVisibility(visible = showFab) {
                FloatingActionButton(
                    onClick = { topLevelBackStack.add(SubNavRoute.Companion.HabitsCreate) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add_a_habit),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = topLevelBackStack.backStack,
            onBack = { topLevelBackStack.removeLast() },
            entryProvider =
                entryProvider {
                    entry<NavRoute.Companion.Habits> {
                        HabitsScreen(
                            onViewLogs = { topLevelBackStack.add(SubNavRoute.Companion.HabitsViewLogs) },
                        )
                    }

                    entry<SubNavRoute.Companion.HabitsCreate> {
                        CreateScreen(onNavigateBack = { topLevelBackStack.removeLast() })
                    }

                    entry<SubNavRoute.Companion.HabitsViewLogs> {
                        Greeting("View Habit Logs")
                    }

                    entry<NavRoute.Companion.Streaks> {
                        Greeting("Streaks")
                    }

                    entry<NavRoute.Companion.Pomodoro> {
                        Greeting("Pomodoro")
                    }

                    entry<NavRoute.Companion.Settings> {
                        Greeting("Settings")
                    }
                },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppPreview() {
    TrackAHabitTheme {
        App(
            topLevelBackStack = remember { TopLevelBackStack(NavRoute.Companion.Habits) },
        )
    }
}
