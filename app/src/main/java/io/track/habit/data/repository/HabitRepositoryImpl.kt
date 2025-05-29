package io.track.habit.data.repository

import io.track.habit.data.local.database.dao.HabitDao
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject // If using Hilt for Dependency Injection

class HabitRepositoryImpl
    @Inject
    constructor(
        private val habitDao: HabitDao,
    ) : HabitRepository {
        override suspend fun insertHabit(habit: Habit): Long {
            return habitDao.insertHabit(habit)
        }

        override suspend fun deleteHabit(habit: Habit) {
            habitDao.deleteHabit(habit)
        }

        override suspend fun updateHabit(habit: Habit) {
            habitDao.updateHabit(habit)
        }

        override fun getAllHabits(): Flow<List<Habit>> {
            return habitDao.getAllHabits()
        }

        override suspend fun getHabitById(habitId: Long): Habit? {
            return habitDao.getHabitById(habitId)
        }

        override fun getHabitByIdFlow(habitId: Long): Flow<Habit?> {
            return habitDao.getHabitByIdFlow(habitId)
        }
    }
