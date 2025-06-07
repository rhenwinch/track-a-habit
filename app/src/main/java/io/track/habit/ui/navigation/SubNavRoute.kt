package io.track.habit.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface SubNavRoute : NavKey {
    val parentRoute: NavRoute
    val name: String

    companion object {
        @Serializable
        data object HabitsCreate : SubNavRoute {
            override val parentRoute: NavRoute = NavRoute.Companion.Habits
            override val name: String = "habits_create"
        }

        @Serializable
        data object HabitsViewLogs : SubNavRoute {
            override val parentRoute: NavRoute = NavRoute.Companion.Habits
            override val name: String = "habits_view_logs"
        }
    }
}
