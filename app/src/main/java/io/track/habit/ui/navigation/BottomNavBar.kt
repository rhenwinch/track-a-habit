package io.track.habit.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import io.track.habit.ui.navigation.NavRoute.Companion.TOP_LEVEL_ROUTES
import io.track.habit.ui.theme.TrackAHabitTheme

@Composable
fun BottomNavBar(
    backStack: TopLevelBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
    ) {
        TOP_LEVEL_ROUTES.forEach { route ->
            val isSelected = route == backStack.topLevelKey

            NavigationBarItem(
                selected = isSelected,
                onClick = { backStack.addTopLevel(route) },
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
        val backStack = remember { TopLevelBackStack<NavKey>(TOP_LEVEL_ROUTES.first()) }

        BottomNavBar(backStack = backStack)
    }
}
