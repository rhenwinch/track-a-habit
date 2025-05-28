package io.track.habit.repository.fake

import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.data.local.database.entities.HabitWithLogs
import io.track.habit.domain.repository.HabitLogsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

/**
 * Fake implementation of HabitLogsRepository for unit testing.
 * This implementation stores data in memory and provides reactive flows.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FakeHabitLogsRepository : HabitLogsRepository {
    private val habitLogs = mutableMapOf<Long, HabitLog>()
    private val idGenerator = AtomicLong(0)

    // StateFlows to emit changes reactively
    private val logsStateFlow = MutableStateFlow(habitLogs.values.toList())

    private fun updateStateFlow() {
        logsStateFlow.value = habitLogs.values.toList()
    }

    override suspend fun insertHabitLog(habitLog: HabitLog): Long {
        val id = idGenerator.incrementAndGet()
        val logWithId = habitLog.copy(logId = id)
        habitLogs[id] = logWithId
        updateStateFlow()
        return id
    }

    override suspend fun getHabitLogById(logId: Long): HabitLog? {
        return habitLogs[logId]
    }

    override suspend fun updateHabitLog(habitLog: HabitLog) {
        habitLogs[habitLog.logId] = habitLog
        updateStateFlow()
    }

    override fun getHabitLogsByHabitId(habitId: Long): Flow<List<HabitLog>> {
        return logsStateFlow.mapLatest { logs ->
            logs
                .filter { it.habitId == habitId }
                .sortedByDescending { it.createdAt.time }
        }
    }

    override fun getLongestStreakForHabit(habitId: Long): Flow<HabitLog?> {
        return logsStateFlow.map { logs ->
            logs
                .filter { it.habitId == habitId }
                .maxByOrNull { it.streakDuration }
        }
    }

    override fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs?> {
        return logsStateFlow.mapLatest { logs ->
            // Create a mock habit for testing
            val habit =
                Habit(
                    habitId = habitId,
                    name = "Test Habit",
                    isActive = true,
                    createdAt = Date(),
                    updatedAt = Date(),
                )

            val habitLogs =
                logs
                    .filter { it.habitId == habitId }
                    .sortedByDescending { it.createdAt.time }

            if (habitLogs.isNotEmpty() || habitId <= 2L) { // Allow for test habits
                HabitWithLogs(habit = habit, logs = habitLogs)
            } else {
                null
            }
        }
    }
}
