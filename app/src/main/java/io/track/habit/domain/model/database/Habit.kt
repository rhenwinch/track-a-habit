package io.track.habit.domain.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a habit being tracked by a user
 *
 * @property habitId Unique identifier for the habit
 * @property name The name of the habit
 * @property createdAt The date when the habit was first started
 * @property isActive Whether the habit is currently active
 * @property updatedAt The date when the habit was last updated
 *
 * @see HabitLog
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val habitId: Int = 0,
    val name: String,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
)
