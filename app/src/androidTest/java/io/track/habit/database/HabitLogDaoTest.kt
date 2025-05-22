package io.track.habit.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.track.habit.data.database.AppDatabase
import io.track.habit.data.database.dao.HabitLogDao
import io.track.habit.domain.model.database.HabitLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Date

/**
 * Instrumented test for HabitLogDao.
 *
 * Tests the data access operations for HabitLog entities based on the provided entity structure
 * and available DAO methods.
 */
@RunWith(AndroidJUnit4::class)
class HabitLogDaoTest {

    private lateinit var habitLogDao: HabitLogDao
    private lateinit var db: AppDatabase // Assuming your AppDatabase class is named AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .build()
        habitLogDao = db.habitLogDao() // Assuming AppDatabase has a habitLogDao() method
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetHabitLogById() = runBlocking {
        val habitLog = HabitLog(
            habitId = 1,
            streakDuration = 5,
            createdAt = Date(),
            updatedAt = Date(),
            trigger = "Evening",
            notes = "Had a great day"
        )
        habitLogDao.insertHabitLog(habitLog)

        // Retrieve logs for the habitId and check if the inserted log is there
        val logsForHabit = habitLogDao.getHabitLogsByHabitId(1).first()
        assertEquals(logsForHabit.size, 1)
        val retrievedLog = logsForHabit[0]

        assertEquals(retrievedLog.habitId, 1)
        assertEquals(retrievedLog.streakDuration, 5)
        assertEquals(retrievedLog.trigger, "Evening")
        assertEquals(retrievedLog.notes, "Had a great day")
        assertNotNull(retrievedLog.createdAt)
        assertNotNull(retrievedLog.updatedAt)
        // Again, be cautious with exact Date comparisons unless necessary
    }

    @Test
    @Throws(Exception::class)
    fun getHabitLogsByHabitId_MultipleLogs() = runBlocking {
        val habitId1 = 1L
        val habitId2 = 2L

        val habitLog1 = HabitLog(habitId = habitId1, streakDuration = 1)
        val habitLog2 = HabitLog(habitId = habitId1, streakDuration = 2)
        val habitLog3 = HabitLog(habitId = habitId2, streakDuration = 3)

        habitLogDao.insertHabitLog(habitLog1)
        habitLogDao.insertHabitLog(habitLog2)
        habitLogDao.insertHabitLog(habitLog3)

        val logsForHabit1 = habitLogDao.getHabitLogsByHabitId(habitId1).first()
        assertEquals(logsForHabit1.size, 2)
        // Optional: Add checks to verify the contents of logsForHabit1

        val logsForHabit2 = habitLogDao.getHabitLogsByHabitId(habitId2).first()
        assertEquals(logsForHabit2.size, 1)
    }

    @Test
    @Throws(Exception::class)
    fun updateHabitLog() = runBlocking {
        val habitId = 20L
        val initialLog = HabitLog(habitId = habitId, streakDuration = 5, notes = "Initial notes")
        habitLogDao.insertHabitLog(initialLog)

        // Retrieve the inserted log to get the auto-generated logId
        val logsForHabit = habitLogDao.getHabitLogsByHabitId(habitId).first()
        assertEquals(logsForHabit.size, 1)
        val retrievedLog = logsForHabit[0]

        val updatedLog = retrievedLog.copy(streakDuration = 7, notes = "Updated notes", updatedAt = Date())

        habitLogDao.updateHabitLog(updatedLog)

        val logsAfterUpdate = habitLogDao.getHabitLogsByHabitId(habitId).first()
        assertEquals(logsAfterUpdate.size, 1)
        val finalLog = logsAfterUpdate[0]

        assertEquals(finalLog.streakDuration, 7)
        assertEquals(finalLog.notes, "Updated notes")
        // Again, be mindful of exact date comparisons
    }

    @Test
    @Throws(Exception::class)
    fun getHabitLogById() = runBlocking {
        val habitLog = HabitLog(habitId = 30, streakDuration = 15, notes = "Specific log")
        habitLogDao.insertHabitLog(habitLog)

        val logsForHabit = habitLogDao.getHabitLogsByHabitId(30).first()
        assertEquals(logsForHabit.size, 1)
        val insertedLog = logsForHabit[0]

        val retrievedLog = habitLogDao.getHabitLogById(insertedLog.logId)

        assertNotNull(retrievedLog)
        assertEquals(retrievedLog?.logId, insertedLog.logId)
        assertEquals(retrievedLog?.habitId, 30)
        assertEquals(retrievedLog?.streakDuration, 15)
        assertEquals(retrievedLog?.notes, "Specific log")
    }

    @Test
    @Throws(Exception::class)
    fun getHabitWithLongestStreak() = runBlocking {
        val habitId = 1L
        for (i in 1..5) {
            habitLogDao.insertHabitLog(HabitLog(habitId = habitId, streakDuration = i * 10))
        }

        val longestStreakHabit = habitLogDao.getLongestStreakForHabit(habitId).first()

        assertNotNull(longestStreakHabit)
        assertEquals(longestStreakHabit?.habitId, habitId)
        assertEquals(longestStreakHabit?.streakDuration, 50)
    }
}
