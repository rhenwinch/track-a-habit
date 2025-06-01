package io.track.habit.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import io.track.habit.ui.navigation.BottomNavBar
import io.track.habit.ui.navigation.NavRoute
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
    val backStack = rememberNavBackStack(NavRoute.Companion.Habits)

    Scaffold(
        bottomBar = {
            BottomNavBar(backStack = backStack)
        },
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.consumeWindowInsets(innerPadding),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider =
                entryProvider {
                    entry<NavRoute.Companion.Habits> {
                        Greeting("Habits")
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
fun GreetingPreview() {
    TrackAHabitTheme {
        App()
    }
}
