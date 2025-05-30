package io.track.habit.domain.model

import io.track.habit.data.local.database.entities.Habit

data class HabitWithStreak(
    val habit: Habit,
    val streak: Streak,
) {
    val formattedDate: String
        get() = TODO()
}
