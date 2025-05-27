package io.track.habit.domain.repository

import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.data.local.database.entities.HabitWithLogs
import kotlinx.coroutines.flow.Flow

interface HabitLogsRepository {
    fun getHabitLogsByHabitId(habitId: Long): Flow<List<HabitLog>>

    fun getLongestStreakForHabit(habitId: Long): Flow<HabitLog?>

    fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs?>

    suspend fun getHabitLogById(logId: Long): HabitLog?

    suspend fun insertHabitLog(habitLog: HabitLog)

    suspend fun updateHabitLog(habitLog: HabitLog)
}
