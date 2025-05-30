package io.track.habit.data.local.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a habit being tracked by a user
 *
 * @property habitId Unique identifier for the habit
 * @property name The name of the habit
 * @property createdAt The date when the habit was first started
 * @property lastResetAt The date when the habit was last reset
 *
 * @see HabitLog
 */
@Entity(
    tableName = "habits",
    indices = [Index(value = ["name"], unique = true)],
)
data class Habit(
    @PrimaryKey(autoGenerate = true) val habitId: Long = 0,
    val name: String,
    val createdAt: Date = Date(),
    val lastResetAt: Date = Date(),
) {
    @get:Ignore
    val streakInDays: Int
        get() {
            val currentDate = Date().time
            val lastResetAt = lastResetAt.time
            val daysSinceStart = (currentDate - lastResetAt) / (1000 * 60 * 60 * 24)

            return daysSinceStart.toInt()
        }
}
