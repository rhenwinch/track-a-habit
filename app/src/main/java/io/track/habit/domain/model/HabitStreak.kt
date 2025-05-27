package io.track.habit.domain.model

import io.track.habit.data.local.database.entities.Habit

data class HabitStreak(
    val habit: Habit,
    val currentStreak: Streak,
    val daysCompleted: Int,
    val progressPercentage: Float
)
