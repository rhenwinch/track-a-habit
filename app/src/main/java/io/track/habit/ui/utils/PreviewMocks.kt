package io.track.habit.ui.utils

import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.model.Quote
import io.track.habit.domain.model.Streak
import io.track.habit.domain.utils.stringLiteral
import io.track.habit.ui.screens.streaks.StreakSummary
import java.util.Date

object PreviewMocks {
    fun getStreak(suffix: String = "") =
        Streak(
            title = "Mock Streak${if (suffix.isNotBlank()) " $suffix" else ""}",
            minDaysRequired = 0,
            maxDaysRequired = 7,
            badgeIcon = "habit_logs",
            message = "",
        )

    fun getHabit(
        habitId: Long = 0,
        date: Date = Date(),
        suffix: String = "",
    ) = Habit(
        habitId = habitId,
        name = "Mock Habit${if (suffix.isNotBlank()) " $suffix" else ""}",
        lastResetAt = date,
    )

    fun getQuote(
        message: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris iaculis  sollicitudin nibh.",
        author: String = "Anonymous",
    ) = Quote(
        message = message,
        author = author,
    )

    fun getStreakSummary(
        streak: Streak = getStreak(),
        status: String = "Active",
        durationText: String = "7 days - 15 days",
        isAchieved: Boolean = true,
    ) = StreakSummary(
        title = stringLiteral(streak.title),
        status = stringLiteral(status),
        durationText = stringLiteral(durationText),
        isAchieved = isAchieved,
        badgeIcon = streak.badgeIcon,
    )
}
