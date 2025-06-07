package io.track.habit.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import io.track.habit.ui.navigation.BottomNavBar
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
    Scaffold(
        bottomBar = {
            BottomNavBar(backStack = topLevelBackStack)
        },
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.consumeWindowInsets(innerPadding),
            backStack = topLevelBackStack.backStack,
            onBack = { topLevelBackStack.removeLast() },
            entryProvider =
                entryProvider {
                    entry<NavRoute.Companion.Habits> {
                        HabitsScreen(
                            onAddHabit = { topLevelBackStack.add(SubNavRoute.Companion.HabitsCreate) },
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
