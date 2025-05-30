package io.track.habit.domain.repository

import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.utils.SortOrder
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun insertHabit(habit: Habit): Long

    suspend fun deleteHabit(habit: Habit)

    suspend fun updateHabit(habit: Habit)

    fun getAllHabits(sortOrder: SortOrder = SortOrder.Creation()): Flow<List<Habit>>

    suspend fun getHabitById(habitId: Long): Habit?

    fun getHabitByIdFlow(habitId: Long): Flow<Habit?>
}
