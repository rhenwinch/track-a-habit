package io.track.habit.ui.utils

import io.track.habit.R
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.domain.model.Quote
import io.track.habit.domain.model.Streak
import io.track.habit.domain.utils.drawableRes
import io.track.habit.domain.utils.stringLiteral
import io.track.habit.ui.screens.streaks.StreakSummary
import java.util.Date

object PreviewMocks {
    fun getStreak(suffix: String = "") =
        Streak(
            title = stringLiteral("Mock Streak${if (suffix.isNotBlank()) " $suffix" else ""}"),
            minDaysRequired = 0,
            maxDaysRequired = 7,
            badgeIcon = drawableRes(R.drawable.you_rock_emoji),
            message = stringLiteral("Wow! You really did it! Keep it up and continue your streak!"),
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
        status: String = "5 habits are in this streak",
        durationText: String = "7 days - 15 days",
        isAchieved: Boolean = true,
    ) = StreakSummary(
        title = streak.title,
        status = stringLiteral(status),
        durationText = stringLiteral(durationText),
        isAchieved = isAchieved,
        badgeIcon = streak.badgeIcon,
        message = streak.message,
    )

    fun getHabitLog(
        habitId: Long = 0,
        streakDuration: Int = 10,
        date: Date = Date(),
        notes: String = "Mock log notes",
    ) = HabitLog(
        logId = 0,
        habitId = habitId,
        streakDuration = streakDuration,
        createdAt = date,
        updatedAt = date,
        trigger = null,
        notes = notes,
    )
}
