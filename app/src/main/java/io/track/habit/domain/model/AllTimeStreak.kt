package io.track.habit.domain.model

import androidx.compose.runtime.Stable
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.domain.utils.formatActiveSinceDate
import io.track.habit.domain.utils.formatDateDuration
import java.util.Date

/**
 * Represents a complete streak record for a habit, containing both the metadata about the streak
 * achievement (badge, title, etc.) and the time period details.
 *
 * This class tracks both ongoing and completed streaks, as determined by whether [endDate] is null.
 *
 * @property streakInDays The duration of the streak in days
 * @property streak The achievement metadata associated with this streak (badge, title, etc.)
 * @property startDate The date when this streak began
 * @property endDate The date when this streak ended, or null if it's still active
 */
@Stable
data class AllTimeStreak(
    val streakInDays: Int,
    val streak: Streak,
    val startDate: Date,
    val endDate: Date?,
) {
    /**
     * Provides a formatted string representation of the streak's duration
     * based on whether the streak is active or completed.
     */
    val formattedDateDuration: String
        get() =
            if (endDate != null) {
                formatDateDuration(
                    startDate = startDate,
                    endDate = endDate,
                )
            } else {
                startDate.formatActiveSinceDate()
            }

    companion object {
        /**
         * Creates an AllTimeStreak from an ongoing habit with streak information
         */
        fun HabitWithStreak.toAllTimeStreak(): AllTimeStreak {
            return AllTimeStreak(
                streakInDays = habit.streakInDays,
                streak = streak,
                startDate = habit.lastResetAt,
                endDate = null,
            )
        }

        /**
         * Creates an AllTimeStreak from a completed habit log record
         */
        fun HabitLog.toAllTimeStreak(streak: Streak): AllTimeStreak {
            return AllTimeStreak(
                streakInDays = streakDuration,
                streak = streak,
                startDate = createdAt,
                endDate = createdAt,
            )
        }
    }
}
