package io.track.habit.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import io.track.habit.ui.theme.TrackAHabitTheme

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    backStack: NavBackStack,
) {
    NavigationBar(
        modifier = modifier,
    ) {
        NavRoute.TOP_LEVEL_ROUTES.forEach { route ->
            val isSelected = route == backStack.lastOrNull()

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    backStack.add(route)
                },
                label = {
                    Text(
                        text = route.name.asString(),
                        maxLines = 1,
                    )
                },
                icon = {
                    AnimatedContent(isSelected) {
                        val painter =
                            if (it) {
                                route.selectedIcon.asPainter()
                            } else {
                                route.unselectedIcon.asPainter()
                            }

                        Icon(
                            painter = painter,
                            contentDescription = route.name.asString(),
                        )
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun BottomNavBarPreview() {
    TrackAHabitTheme {
        val backStack = rememberNavBackStack(NavRoute.Companion.Habits)

        BottomNavBar(backStack = backStack)
    }
}
