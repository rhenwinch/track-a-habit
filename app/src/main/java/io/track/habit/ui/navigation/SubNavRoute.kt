package io.track.habit.ui.navigation

import kotlinx.serialization.Serializable

sealed interface SubNavRoute : NavRoute {

    @Serializable
    data object HabitsCreate : SubNavRoute

    @Serializable
    data class HabitsViewLogs(
        val habitId: Long,
    ) : SubNavRoute
}
