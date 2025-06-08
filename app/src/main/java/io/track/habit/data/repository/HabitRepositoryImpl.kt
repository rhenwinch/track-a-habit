package io.track.habit.data.repository

import io.track.habit.data.local.database.dao.HabitDao
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.utils.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
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

        override fun getLongestStreakInDays(): Flow<Int> {
            return habitDao.getLongestStreakInDays()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun getAllHabits(sortOrder: SortOrder): Flow<List<Habit>> {
            return when (sortOrder) {
                is SortOrder.Streak -> {
                    val habits = habitDao.getAllHabitsSortedByNameAsc()
                    if (sortOrder.ascending) {
                        habits.mapLatest { it.sortedBy { it.streakInDays } }
                    } else {
                        habits.mapLatest { it.sortedByDescending { it.streakInDays } }
                    }
                }
                is SortOrder.Creation -> {
                    if (sortOrder.ascending) {
                        habitDao.getAllHabitsSortedByCreationDateAsc()
                    } else {
                        habitDao.getAllHabitsSortedByCreationDateDesc()
                    }
                }
                is SortOrder.Name -> {
                    if (sortOrder.ascending) {
                        habitDao.getAllHabitsSortedByNameAsc()
                    } else {
                        habitDao.getAllHabitsSortedByNameDesc()
                    }
                }
            }
        }

        override suspend fun getHabitById(habitId: Long): Habit? {
            return habitDao.getHabitById(habitId)
        }

        override fun getHabitByIdFlow(habitId: Long): Flow<Habit?> {
            return habitDao.getHabitByIdFlow(habitId)
        }
    }
