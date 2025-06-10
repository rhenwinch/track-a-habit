package io.track.habit.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a habit log which has been reset by the user
 *
 * @property logId Unique identifier for the log
 * @property habitId The ID of the habit associated with the log
 * @property createdAt The date and time when the log was created
 * @property trigger The trigger that caused the log to be created
 * @property notes Any additional notes associated with the log
 * @property updatedAt The date and time when the log was last updated
 * @property streakDuration The duration of the streak in days
 *
 * @see Habit
 */
@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId"])],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["habitId"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val habitId: Long,
    val streakDuration: Int,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val trigger: String? = null,
    val notes: String? = null,
)
