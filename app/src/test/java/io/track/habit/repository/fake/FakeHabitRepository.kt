package io.track.habit.repository.fake

import android.database.sqlite.SQLiteConstraintException
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.utils.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import java.util.concurrent.atomic.AtomicLong

/**
 * Fake implementation of HabitRepository for unit testing.
 * This replaces the need for Room database and DAO dependencies.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FakeHabitRepository : HabitRepository {
    private val habits = mutableMapOf<Long, Habit>()
    private val habitNames = mutableSetOf<String>()
    private val idGenerator = AtomicLong(0)

    private val allHabitsFlow = MutableStateFlow<List<Habit>>(emptyList())

    override suspend fun insertHabit(habit: Habit): Long {
        // Check for duplicate names
        if (habitNames.contains(habit.name)) {
            throw SQLiteConstraintException("UNIQUE constraint failed: habit.name")
        }

        val id = idGenerator.incrementAndGet()
        val habitWithId = habit.copy(habitId = id)
        habits[id] = habitWithId
        habitNames.add(habit.name)
        updateAllHabitsFlow()
        return id
    }

    override suspend fun deleteHabit(habit: Habit) {
        val existingHabit = habits[habit.habitId]
        if (existingHabit != null) {
            habits.remove(habit.habitId)
            habitNames.remove(existingHabit.name)
            updateAllHabitsFlow()
        }
    }

    override suspend fun updateHabit(habit: Habit) {
        if (habits.containsKey(habit.habitId)) {
            // Remove old name and add new name for duplicate checking
            val oldHabit = habits[habit.habitId]!!
            habitNames.remove(oldHabit.name)
            habitNames.add(habit.name)

            habits[habit.habitId] = habit
            updateAllHabitsFlow()
        }
    }

    override fun getLongestStreakInDays(): Flow<Int> {
        return allHabitsFlow.map { habits ->
            habits.maxOfOrNull { it.streakInDays } ?: 0
        }
    }

    override fun getAllHabits(sortOrder: SortOrder): Flow<List<Habit>> {
        return allHabitsFlow.mapLatest {
            when (sortOrder) {
                is SortOrder.Name ->
                    if (sortOrder.ascending) {
                        it.sortedBy { habit -> habit.name }
                    } else {
                        it.sortedByDescending { habit -> habit.name }
                    }
                is SortOrder.Creation ->
                    if (sortOrder.ascending) {
                        it.sortedBy { habit -> habit.createdAt }
                    } else {
                        it.sortedByDescending { habit -> habit.createdAt }
                    }
                is SortOrder.Streak ->
                    if (sortOrder.ascending) {
                        it.sortedBy { habit -> habit.streakInDays }
                    } else {
                        it.sortedByDescending { habit -> habit.streakInDays }
                    }
            }
        }
    }

    override suspend fun getHabitById(habitId: Long): Habit? {
        return if (habitId <= 0) null else habits[habitId]
    }

    override fun getHabitByIdFlow(habitId: Long): Flow<Habit?> {
        return allHabitsFlow.map { habits[habitId] }
    }

    private fun updateAllHabitsFlow() {
        allHabitsFlow.value = habits.values.toList()
    }
}
