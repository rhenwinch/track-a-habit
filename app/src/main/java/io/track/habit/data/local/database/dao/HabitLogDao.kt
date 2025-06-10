package io.track.habit.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.data.local.database.entities.HabitWithLogs
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE logId = :logId")
    suspend fun getHabitLogById(logId: Long): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY createdAt DESC")
    fun getHabitLogsByHabitId(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY streakDuration DESC LIMIT 1")
    fun getLongestStreakForHabit(habitId: Long): Flow<HabitLog?>

    @Query("SELECT COALESCE(MAX(streakDuration), 0) FROM habit_logs")
    fun getLongestStreakAchieved(): Flow<Int>

    @Transaction
    @Query("SELECT * FROM habits WHERE habitId = :habitId")
    fun getHabitWithLogsFlow(habitId: Long): Flow<HabitWithLogs?>

    @Query("SELECT * FROM habit_logs ORDER BY streakDuration DESC")
    fun getHabitLogs(): Flow<List<HabitLog>>

    @Insert
    suspend fun insertHabitLog(habitLog: HabitLog): Long

    @Update
    suspend fun updateHabitLog(habitLog: HabitLog)
}
