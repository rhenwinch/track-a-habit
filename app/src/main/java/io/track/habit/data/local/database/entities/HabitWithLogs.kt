package io.track.habit.data.local.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class HabitWithLogs(
    @Embedded val habit: Habit,
    @Relation(
        parentColumn = "habitId",
        entityColumn = "habitId",
    ) val logs: List<HabitLog>,
)
