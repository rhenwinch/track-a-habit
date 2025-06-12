package io.track.habit.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination

private fun NavBackStackEntry.lifecycleIsResumed() = lifecycle.currentState == Lifecycle.State.RESUMED

fun NavController.navigateIfResumed(navRoute: NavRoute) {
    if (currentBackStackEntry?.lifecycleIsResumed() == false) {
        return
    }

    navigate(navRoute) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}

fun NavDestination?.isSelected(route: NavRoute): Boolean {
    if (this == null) return false

    return hierarchy.any { it.hasRoute(route::class) }
}
