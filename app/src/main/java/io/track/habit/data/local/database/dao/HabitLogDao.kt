package io.track.habit.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.track.habit.data.local.database.entities.HabitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE logId = :logId")
    suspend fun getHabitLogById(logId: Long): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId")
    fun getHabitLogsByHabitId(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY streakDuration DESC LIMIT 1")
    fun getLongestStreakForHabit(habitId: Long): Flow<HabitLog?>

    @Insert
    suspend fun insertHabitLog(habitLog: HabitLog)

    @Update
    suspend fun updateHabitLog(habitLog: HabitLog)
}
