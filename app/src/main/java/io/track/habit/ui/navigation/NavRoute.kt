package io.track.habit.ui.navigation

import io.track.habit.domain.utils.DrawableResource
import io.track.habit.domain.utils.StringResource
import io.track.habit.domain.utils.drawableRes
import io.track.habit.domain.utils.stringRes

interface NavRoute {
    val icon: DrawableResource
    val name: StringResource

    companion object {
        data object Habits : NavRoute {
            override val icon: DrawableResource = drawableRes(TODO())
            override val name: StringResource = stringRes(TODO())
        }

        data object Streaks : NavRoute {
            override val icon: DrawableResource = drawableRes(TODO())
            override val name: StringResource = stringRes(TODO())
        }

        data object Pomodoro : NavRoute {
            override val icon: DrawableResource = drawableRes(TODO())
            override val name: StringResource = stringRes(TODO())
        }

        data object Settings : NavRoute {
            override val icon: DrawableResource = drawableRes(TODO())
            override val name: StringResource = stringRes(TODO())
        }

        val TOP_LEVEL_ROUTES =
            listOf(
                Habits,
                Streaks,
                Pomodoro,
                Settings,
            )
    }
}
