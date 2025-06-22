package io.track.habit.ui.navigation

import io.track.habit.R
import io.track.habit.domain.utils.DrawableResource
import io.track.habit.domain.utils.StringResource
import io.track.habit.domain.utils.drawableRes
import io.track.habit.domain.utils.stringLiteral
import io.track.habit.domain.utils.stringRes
import kotlinx.serialization.Serializable

/**
 * Represents a top navigation route in the application.
 * Each route has an icon and a name, which are defined as drawable and string resources respectively.
 *
 * The companion object defines the specific routes available in the app:
 * - Habits: Route for the habits screen.
 * - Streaks: Route for the streaks screen.
 * - Pomodoro: Route for the pomodoro timer screen.
 * - Settings: Route for the settings screen.
 *
 * It also provides a list of top-level routes, `TOP_LEVEL_ROUTES`, which can be used for navigation UI elements like bottom navigation bars.
 */
sealed interface TopNavRoute : NavRoute {
    val unselectedIcon: DrawableResource
    val selectedIcon: DrawableResource
    val name: StringResource

    @Serializable
    data object Habits : TopNavRoute {
        override val unselectedIcon: DrawableResource = drawableRes(R.drawable.habits_outlined)
        override val selectedIcon: DrawableResource = drawableRes(R.drawable.habits_filled)
        override val name: StringResource = stringRes(R.string.habits)
    }

    @Serializable
    data object Streaks : TopNavRoute {
        override val unselectedIcon: DrawableResource = drawableRes(R.drawable.streak_outlined)
        override val selectedIcon: DrawableResource = drawableRes(R.drawable.streak_filled)
        override val name: StringResource = stringRes(R.string.streaks)
    }

    @Serializable
    data object Settings : TopNavRoute {
        override val unselectedIcon: DrawableResource = drawableRes(R.drawable.settings_outlined)
        override val selectedIcon: DrawableResource = drawableRes(R.drawable.settings_filled)
        override val name: StringResource = stringRes(R.string.settings)
    }

    @Serializable
    data object Onboarding : TopNavRoute {
        override val unselectedIcon: DrawableResource = drawableRes(-1)
        override val selectedIcon: DrawableResource = drawableRes(-1)
        override val name: StringResource = stringLiteral("")
    }

    companion object {
        val TOP_LEVEL_ROUTES =
            listOf(
                Habits,
                Streaks,
                Settings,
            )
    }
}
