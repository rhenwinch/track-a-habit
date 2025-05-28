package io.track.habit.repository.fake

import android.database.sqlite.SQLiteConstraintException
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

/**
 * Fake implementation of HabitRepository for unit testing.
 * This replaces the need for Room database and DAO dependencies.
 */
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

    override fun getAllHabits(): Flow<List<Habit>> = allHabitsFlow

    override suspend fun getHabitById(habitId: Long): Habit? {
        return if (habitId <= 0) null else habits[habitId]
    }

    override fun getHabitByIdFlow(habitId: Long): Flow<Habit?> {
        return allHabitsFlow.map { habits[habitId] }
    }

    override fun getActiveHabits(): Flow<List<Habit>> {
        return allHabitsFlow.map { list -> list.filter { it.isActive } }
    }

    override suspend fun setHabitInactive(habitId: Long) {
        val habit = habits[habitId]
        if (habit != null) {
            habits[habitId] = habit.copy(isActive = false, updatedAt = Date())
            updateAllHabitsFlow()
        }
    }

    private fun updateAllHabitsFlow() {
        allHabitsFlow.value = habits.values.toList()
    }
}
