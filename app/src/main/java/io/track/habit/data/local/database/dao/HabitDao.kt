package io.track.habit.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.track.habit.data.local.database.entities.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("SELECT * FROM habits ORDER BY name ASC")
    fun getAllHabitsSortedByNameAsc(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY name DESC")
    fun getAllHabitsSortedByNameDesc(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabitsSortedByCreationDateAsc(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabitsSortedByCreationDateDesc(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE habitId = :habitId")
    suspend fun getHabitById(habitId: Long): Habit?

    @Query("SELECT * FROM habits WHERE habitId = :habitId")
    fun getHabitByIdFlow(habitId: Long): Flow<Habit?>

    @Query(
        "SELECT COALESCE(MAX((strftime('%s', 'now') - strftime('%s', datetime(lastResetAt/1000, 'unixepoch'))) / 86400), 0) FROM habits WHERE lastResetAt > 0",
    )
    fun getLongestStreakInDays(): Flow<Int>
}
