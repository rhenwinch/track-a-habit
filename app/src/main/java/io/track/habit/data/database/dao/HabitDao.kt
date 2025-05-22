package io.track.habit.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.track.habit.domain.model.database.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE habitId = :habitId")
    suspend fun getHabitById(habitId: Long): Habit?

    @Query("SELECT * FROM habits WHERE habitId = :habitId")
    fun getHabitByIdFlow(habitId: Long): Flow<Habit?>

    @Query("SELECT * FROM habits WHERE isActive = 1")
    fun getActiveHabits(): Flow<List<Habit>>

    @Query("UPDATE habits SET isActive = 0 WHERE habitId = :habitId")
    suspend fun setHabitInactive(habitId: Long)
}
