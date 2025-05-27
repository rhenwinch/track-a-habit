package io.track.habit.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import io.track.habit.data.local.database.AppDatabase
import io.track.habit.data.local.database.dao.HabitDao
import io.track.habit.data.local.database.dao.HabitLogDao
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.data.repository.HabitLogsRepositoryImpl
import io.track.habit.domain.repository.HabitLogsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HabitLogsRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var habitDao: HabitDao
    private lateinit var habitLogDao: HabitLogDao
    private lateinit var repository: HabitLogsRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(
                    context,
                    AppDatabase::class.java,
                ).allowMainThreadQueries()
                .build()

        habitDao = database.habitDao()
        habitLogDao = database.habitLogDao()
        repository = HabitLogsRepositoryImpl(habitLogDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertHabitLog_insertsLogSuccessfully() =
        runTest {
            val habit = createTestHabit(name = "Exercise")
            val habitId = habitDao.insertHabit(habit)

            val habitLog =
                HabitLog(
                    habitId = habitId,
                    streakDuration = 5,
                    trigger = "Morning alarm",
                    notes = "Felt great today",
                )

            repository.insertHabitLog(habitLog)

            val retrievedLog = repository.getHabitLogById(1L)
            assertNotNull(retrievedLog)
            assertEquals(habitId, retrievedLog?.habitId)
            assertEquals(5, retrievedLog?.streakDuration)
            assertEquals("Morning alarm", retrievedLog?.trigger)
            assertEquals("Felt great today", retrievedLog?.notes)
        }

    @Test
    fun getHabitLogById_existingLog_returnsLog() =
        runTest {
            val habit = createTestHabit(name = "Reading")
            val habitId = habitDao.insertHabit(habit)

            val habitLog =
                HabitLog(
                    habitId = habitId,
                    streakDuration = 3,
                    trigger = "Before bed",
                    notes = "Read 20 pages",
                )
            repository.insertHabitLog(habitLog)

            val result = repository.getHabitLogById(1L)

            assertNotNull(result)
            assertEquals(habitId, result?.habitId)
            assertEquals(3, result?.streakDuration)
            assertEquals("Before bed", result?.trigger)
            assertEquals("Read 20 pages", result?.notes)
        }

    @Test
    fun getHabitLogById_nonExistentLog_returnsNull() =
        runTest {
            val result = repository.getHabitLogById(999L)

            assertNull(result)
        }

    @Test
    fun updateHabitLog_updatesLogSuccessfully() =
        runTest {
            val habit = createTestHabit(name = "Meditation")
            val habitId = habitDao.insertHabit(habit)

            val originalLog =
                HabitLog(
                    habitId = habitId,
                    streakDuration = 2,
                    trigger = "After work",
                    notes = "5 minutes",
                )
            repository.insertHabitLog(originalLog)

            val updatedLog =
                HabitLog(
                    logId = 1L,
                    habitId = habitId,
                    streakDuration = 7,
                    trigger = "Morning routine",
                    notes = "10 minutes meditation",
                    createdAt = originalLog.createdAt,
                    updatedAt = Date(),
                )

            repository.updateHabitLog(updatedLog)

            val result = repository.getHabitLogById(1L)
            assertNotNull(result)
            assertEquals(7, result?.streakDuration)
            assertEquals("Morning routine", result?.trigger)
            assertEquals("10 minutes meditation", result?.notes)
        }

    @Test
    fun getHabitLogsByHabitId_returnsLogsInDescendingOrder() =
        runTest {
            val habit = createTestHabit(name = "Water Intake")
            val habitId = habitDao.insertHabit(habit)

            val log1 =
                HabitLog(
                    habitId = habitId,
                    streakDuration = 1,
                    createdAt = Date(System.currentTimeMillis() - 86400000), // 1 day ago
                )
            val log2 =
                HabitLog(
                    habitId = habitId,
                    streakDuration = 2,
                    createdAt = Date(System.currentTimeMillis() - 43200000), // 12 hours ago
                )
            val log3 =
                HabitLog(
                    habitId = habitId,
                    streakDuration = 3,
                    createdAt = Date(), // now
                )

            repository.insertHabitLog(log1)
            repository.insertHabitLog(log2)
            repository.insertHabitLog(log3)

            repository.getHabitLogsByHabitId(habitId).test {
                val logs = awaitItem()
                assertEquals(3, logs.size)
                // Should be ordered by createdAt DESC (most recent first)
                assertEquals(3, logs[0].streakDuration) // Most recent
                assertEquals(2, logs[1].streakDuration)
                assertEquals(1, logs[2].streakDuration) // Oldest
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun getHabitLogsByHabitId_emptyHabit_returnsEmptyList() =
        runTest {
            val habit = createTestHabit(name = "Empty Habit")
            val habitId = habitDao.insertHabit(habit)

            repository.getHabitLogsByHabitId(habitId).test {
                val logs = awaitItem()
                assertTrue(logs.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun getLongestStreakForHabit_returnsLogWithHighestStreakDuration() =
        runTest {
            val habit = createTestHabit(name = "Exercise")
            val habitId = habitDao.insertHabit(habit)

            val log1 = HabitLog(habitId = habitId, streakDuration = 5)
            val log2 = HabitLog(habitId = habitId, streakDuration = 10) // Longest
            val log3 = HabitLog(habitId = habitId, streakDuration = 3)

            repository.insertHabitLog(log1)
            repository.insertHabitLog(log2)
            repository.insertHabitLog(log3)

            repository.getLongestStreakForHabit(habitId).test {
                val longestStreak = awaitItem()
                assertNotNull(longestStreak)
                assertEquals(10, longestStreak?.streakDuration)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun getLongestStreakForHabit_noLogs_returnsNull() =
        runTest {
            val habit = createTestHabit(name = "No Logs Habit")
            val habitId = habitDao.insertHabit(habit)

            repository.getLongestStreakForHabit(habitId).test {
                val longestStreak = awaitItem()
                assertNull(longestStreak)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun getHabitWithLogs_existingHabitWithLogs_returnsHabitWithLogs() =
        runTest {
            val habit = createTestHabit(name = "Journaling")
            val habitId = habitDao.insertHabit(habit)

            val log1 = HabitLog(habitId = habitId, streakDuration = 2, notes = "Morning pages")
            val log2 = HabitLog(habitId = habitId, streakDuration = 4, notes = "Evening reflection")

            repository.insertHabitLog(log1)
            repository.insertHabitLog(log2)

            repository.getHabitWithLogs(habitId).test {
                val result = awaitItem()

                assertNotNull(result)
                assertEquals("Journaling", result?.habit?.name)
                assertEquals(2, result?.logs?.size)
                assertEquals("Evening reflection", result?.logs?.get(0)?.notes)
                assertEquals("Morning pages", result?.logs?.get(1)?.notes)
            }
        }

    @Test
    fun getHabitWithLogs_existingHabitNoLogs_returnsHabitWithEmptyLogs() =
        runTest {
            val habit = createTestHabit(name = "New Habit")
            val habitId = habitDao.insertHabit(habit)

            repository.getHabitWithLogs(habitId).test {
                val result = awaitItem()

                assertNotNull(result)
                assertEquals("New Habit", result?.habit?.name)
                assertTrue(result?.logs?.isEmpty() == true)
            }
        }

    @Test
    fun getHabitWithLogs_nonExistentHabit_returnsNull() =
        runTest {
            repository.getHabitWithLogs(999L).test {
                val result = awaitItem()
                assertNull(result)
            }
        }

    @Test
    fun flowUpdates_whenNewLogAdded_emitsNewData() =
        runTest {
            val habit = createTestHabit(name = "Walking")
            val habitId = habitDao.insertHabit(habit)

            repository.getHabitLogsByHabitId(habitId).test {
                // Initial empty state
                val initialLogs = awaitItem()
                assertTrue(initialLogs.isEmpty())

                // Add first log
                val log1 = HabitLog(habitId = habitId, streakDuration = 1)
                repository.insertHabitLog(log1)

                val logsAfterFirst = awaitItem()
                assertEquals(1, logsAfterFirst.size)
                assertEquals(1, logsAfterFirst[0].streakDuration)

                // Add second log
                val log2 = HabitLog(habitId = habitId, streakDuration = 2)
                repository.insertHabitLog(log2)

                val logsAfterSecond = awaitItem()
                assertEquals(2, logsAfterSecond.size)
                // Most recent first due to ORDER BY createdAt DESC
                assertEquals(2, logsAfterSecond[0].streakDuration)
                assertEquals(1, logsAfterSecond[1].streakDuration)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun longestStreakFlow_whenNewLongestStreakAdded_emitsUpdatedData() =
        runTest {
            val habit = createTestHabit(name = "Push-ups")
            val habitId = habitDao.insertHabit(habit)

            repository.getLongestStreakForHabit(habitId).test {
                // Initial null state
                val initialLongest = awaitItem()
                assertNull(initialLongest)

                // Add first log
                val log1 = HabitLog(habitId = habitId, streakDuration = 5)
                repository.insertHabitLog(log1)

                val longestAfterFirst = awaitItem()
                assertEquals(5, longestAfterFirst?.streakDuration)

                // Add shorter streak (shouldn't change longest)
                val log2 = HabitLog(habitId = habitId, streakDuration = 3)
                repository.insertHabitLog(log2)

                // Should still be 5 (no emission expected for same longest)
                expectNoEvents()

                // Add longer streak
                val log3 = HabitLog(habitId = habitId, streakDuration = 10)
                repository.insertHabitLog(log3)

                val longestAfterThird = awaitItem()
                assertEquals(10, longestAfterThird?.streakDuration)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun multipleHabits_logsIsolatedCorrectly() =
        runTest {
            val habit1 = createTestHabit(name = "Habit 1")
            val habit2 = createTestHabit(name = "Habit 2")
            val habit1Id = habitDao.insertHabit(habit1)
            val habit2Id = habitDao.insertHabit(habit2)

            val log1ForHabit1 = HabitLog(habitId = habit1Id, streakDuration = 5)
            repository.insertHabitLog(log1ForHabit1)

            val log2ForHabit1 = HabitLog(habitId = habit1Id, streakDuration = 3)
            repository.insertHabitLog(log2ForHabit1)

            val log1ForHabit2 = HabitLog(habitId = habit2Id, streakDuration = 8)
            repository.insertHabitLog(log1ForHabit2)

            repository.getHabitLogsByHabitId(habit1Id).test {
                val habit1Logs = awaitItem()
                assertEquals(2, habit1Logs.size)
                assertEquals(3, habit1Logs[0].streakDuration)
                assertEquals(5, habit1Logs[1].streakDuration)
                cancelAndIgnoreRemainingEvents()
            }

            repository.getHabitLogsByHabitId(habit2Id).test {
                val habit2Logs = awaitItem()
                assertEquals(1, habit2Logs.size)
                assertEquals(8, habit2Logs[0].streakDuration)
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun createTestHabit(
        name: String,
        isActive: Boolean = true,
        createdAt: Date = Date(),
        updatedAt: Date = Date(),
    ) = Habit(
        name = name,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
