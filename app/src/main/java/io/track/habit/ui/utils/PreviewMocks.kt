package io.track.habit.ui.utils

import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.model.Quote
import io.track.habit.domain.model.Streak
import java.util.Date

object PreviewMocks {
    fun getStreak(suffix: String = "") =
        Streak(
            title = "Mock Streak${if (suffix.isNotBlank()) " $suffix" else ""}",
            minDaysRequired = 0,
            maxDaysRequired = 7,
            badgeIcon = "",
            message = "",
        )

    fun getHabit(
        date: Date = Date(),
        suffix: String = "",
    ) = Habit(
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
}
