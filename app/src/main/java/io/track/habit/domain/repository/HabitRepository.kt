package io.track.habit.domain.repository

import io.track.habit.data.local.database.entities.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun insertHabit(habit: Habit): Long

    suspend fun deleteHabit(habit: Habit)

    suspend fun updateHabit(habit: Habit)

    fun getAllHabits(): Flow<List<Habit>>

    suspend fun getHabitById(habitId: Long): Habit?

    fun getHabitByIdFlow(habitId: Long): Flow<Habit?>
}
