package io.track.habit.ui.composables

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.track.habit.ui.navigation.TopNavRoute.Companion.TOP_LEVEL_ROUTES
import io.track.habit.ui.navigation.isSelected
import io.track.habit.ui.navigation.navigateIfResumed
import io.track.habit.ui.theme.TrackAHabitTheme

@SuppressLint("RestrictedApi")
@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = modifier,
    ) {
        TOP_LEVEL_ROUTES.forEach { route ->
            val isSelected = currentDestination?.isSelected(route) ?: false

            NavigationBarItem(
                selected = isSelected,
                onClick = { navController.navigateIfResumed(route) },
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
        BottomNavBar()
    }
}
