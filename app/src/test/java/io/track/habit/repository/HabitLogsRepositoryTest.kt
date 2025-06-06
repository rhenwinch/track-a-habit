package io.track.habit.repository

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.repository.fake.FakeHabitLogsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class HabitLogsRepositoryTest {
    private lateinit var repository: HabitLogsRepository

    @Before
    fun setup() {
        repository = FakeHabitLogsRepository()
    }

    @Test
    fun `when inserting habit log then log is stored successfully`() =
        runTest {
            val habitLog =
                createTestHabitLog(
                    habitId = 1L,
                    streakDuration = 5,
                    trigger = "Morning alarm",
                    notes = "Felt great today",
                )

            repository.insertHabitLog(habitLog)

            val retrievedLog = repository.getHabitLogById(1L)
            assertNotNull(retrievedLog)
            assertEquals(1L, retrievedLog?.habitId)
            assertEquals(5, retrievedLog?.streakDuration)
            assertEquals("Morning alarm", retrievedLog?.trigger)
            assertEquals("Felt great today", retrievedLog?.notes)
        }

    @Test
    fun `when getting habit log by existing id then returns correct log`() =
        runTest {
            val habitLog =
                createTestHabitLog(
                    habitId = 1L,
                    streakDuration = 3,
                    trigger = "Before bed",
                    notes = "Read 20 pages",
                )
            repository.insertHabitLog(habitLog)

            val result = repository.getHabitLogById(1L)

            assertNotNull(result)
            assertEquals(1L, result?.habitId)
            assertEquals(3, result?.streakDuration)
            assertEquals("Before bed", result?.trigger)
            assertEquals("Read 20 pages", result?.notes)
        }

    @Test
    fun `when getting habit log by non-existent id then returns null`() =
        runTest {
            val result = repository.getHabitLogById(999L)

            assertNull(result)
        }

    @Test
    fun `when updating habit log then log is updated successfully`() =
        runTest {
            val originalLog =
                createTestHabitLog(
                    habitId = 1L,
                    streakDuration = 2,
                    trigger = "After work",
                    notes = "5 minutes",
                )
            repository.insertHabitLog(originalLog)

            val updatedLog =
                createTestHabitLog(
                    logId = 1L,
                    habitId = 1L,
                    streakDuration = 7,
                    trigger = "Morning routine",
                    notes = "10 minutes meditation",
                )

            repository.updateHabitLog(updatedLog)

            val result = repository.getHabitLogById(1L)
            assertNotNull(result)
            assertEquals(7, result?.streakDuration)
            assertEquals("Morning routine", result?.trigger)
            assertEquals("10 minutes meditation", result?.notes)
        }

    @Test
    fun `when getting habit logs by habit id then returns logs in descending order by creation date`() =
        runTest {
            val habitId = 1L
            val log1 =
                createTestHabitLog(
                    habitId = habitId,
                    streakDuration = 1,
                    createdAt = Date(System.currentTimeMillis() - 86400000), // 1 day ago
                )
            val log2 =
                createTestHabitLog(
                    habitId = habitId,
                    streakDuration = 2,
                    createdAt = Date(System.currentTimeMillis() - 43200000), // 12 hours ago
                )
            val log3 =
                createTestHabitLog(
                    habitId = habitId,
                    streakDuration = 3,
                    createdAt = Date(), // now
                )

            repository.insertHabitLog(log1)
            repository.insertHabitLog(log2)
            repository.insertHabitLog(log3)

            repository.getHabitLogsByHabitId(habitId).test {
                val logs = expectMostRecentItem()
                assertEquals(3, logs.size)
                assertEquals(3, logs[0].streakDuration) // Most recent
                assertEquals(2, logs[1].streakDuration)
                assertEquals(1, logs[2].streakDuration) // Oldest
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when getting habit logs for habit with no logs then returns empty list`() =
        runTest {
            val habitId = 1L

            repository.getHabitLogsByHabitId(habitId).test {
                val logs = awaitItem()
                assertTrue(logs.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when getting longest streak for habit then returns log with highest streak duration`() =
        runTest {
            val habitId = 1L
            val log1 = createTestHabitLog(habitId = habitId, streakDuration = 5)
            val log2 = createTestHabitLog(habitId = habitId, streakDuration = 10) // Longest
            val log3 = createTestHabitLog(habitId = habitId, streakDuration = 3)

            repository.insertHabitLog(log1)
            repository.insertHabitLog(log2)
            repository.insertHabitLog(log3)

            repository.getLongestStreakForHabit(habitId).test {
                val longestStreak = expectMostRecentItem()
                assertNotNull(longestStreak)
                assertEquals(10, longestStreak?.streakDuration)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when getting longest streak for habit with no logs then returns null`() =
        runTest {
            val habitId = 1L

            repository.getLongestStreakForHabit(habitId).test {
                val longestStreak = awaitItem()
                assertNull(longestStreak)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when getting habit with logs for existing habit then returns habit with logs ordered by creation date`() =
        runTest {
            val habitId = 1L
            val log1 = createTestHabitLog(habitId = habitId, streakDuration = 2, notes = "Morning pages")
            repository.insertHabitLog(log1)

            val log2 =
                createTestHabitLog(
                    habitId = habitId,
                    streakDuration = 4,
                    notes = "Evening reflection",
                    createdAt = Date(System.currentTimeMillis() + 300),
                )
            repository.insertHabitLog(log2)

            repository.getHabitWithLogs(habitId).test {
                val result = expectMostRecentItem()

                assertNotNull(result)
                assertEquals("Test Habit", result?.habit?.name)
                assertEquals(2, result?.logs?.size)
                assertEquals("Evening reflection", result?.logs?.get(0)?.notes)
                assertEquals("Morning pages", result?.logs?.get(1)?.notes)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when getting habit with logs for existing habit with no logs then returns habit with empty logs list`() =
        runTest {
            val habitId = 1L

            repository.getHabitWithLogs(habitId).test {
                val result = awaitItem()

                assertNotNull(result)
                assertEquals("Test Habit", result?.habit?.name)
                assertTrue(result?.logs?.isEmpty() == true)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when getting habit with logs for non-existent habit then returns null`() =
        runTest {
            repository.getHabitWithLogs(999L).test {
                val result = awaitItem()
                assertNull(result)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when observing habit logs flow and adding new logs then emits updated data`() =
        runTest {
            val habitId = 1L

            repository.getHabitLogsByHabitId(habitId).test {
                val log1 = createTestHabitLog(habitId = habitId, streakDuration = 1)
                repository.insertHabitLog(log1)

                val logsAfterFirst = expectMostRecentItem()
                assertEquals(1, logsAfterFirst.size)
                assertEquals(1, logsAfterFirst[0].streakDuration)

                val log2 =
                    createTestHabitLog(
                        habitId = habitId,
                        streakDuration = 2,
                        createdAt = Date(System.currentTimeMillis() + 300),
                    )
                repository.insertHabitLog(log2)

                val logsAfterSecond = awaitItem()
                assertEquals(2, logsAfterSecond.size)
                assertEquals(2, logsAfterSecond[0].streakDuration)
                assertEquals(1, logsAfterSecond[1].streakDuration)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when observing longest streak flow and adding new longest streak then emits updated data`() =
        runTest {
            val habitId = 1L

            repository.getLongestStreakForHabit(habitId).test {
                val log1 = createTestHabitLog(habitId = habitId, streakDuration = 5)
                repository.insertHabitLog(log1)

                val longestAfterFirst = expectMostRecentItem()
                assertEquals(5, longestAfterFirst?.streakDuration)

                val log2 = createTestHabitLog(habitId = habitId, streakDuration = 3)
                repository.insertHabitLog(log2)

                val log3 = createTestHabitLog(habitId = habitId, streakDuration = 10)
                repository.insertHabitLog(log3)

                val longestAfterThird = expectMostRecentItem()
                assertEquals(10, longestAfterThird?.streakDuration)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when working with multiple habits then logs are isolated correctly per habit`() =
        runTest {
            val habit1Id = 1L
            val habit2Id = 2L

            val log1ForHabit1 = createTestHabitLog(habitId = habit1Id, streakDuration = 5)
            repository.insertHabitLog(log1ForHabit1)

            val log1ForHabit2 = createTestHabitLog(habitId = habit2Id, streakDuration = 8)
            repository.insertHabitLog(log1ForHabit2)

            val log2ForHabit1 =
                createTestHabitLog(
                    habitId = habit1Id,
                    streakDuration = 3,
                    createdAt = Date(System.currentTimeMillis() + 4000),
                )
            repository.insertHabitLog(log2ForHabit1)

            turbineScope {
                val habit1Turbine = repository.getHabitLogsByHabitId(habit1Id).testIn(backgroundScope)
                val habit2Turbine = repository.getHabitLogsByHabitId(habit2Id).testIn(backgroundScope)

                with(habit1Turbine) {
                    val habit1Logs = expectMostRecentItem()
                    assertEquals(2, habit1Logs.size)
                    assertEquals(3, habit1Logs[0].streakDuration)
                    assertEquals(5, habit1Logs[1].streakDuration)
                    cancelAndIgnoreRemainingEvents()
                }

                with(habit2Turbine) {
                    val habit2Logs = expectMostRecentItem()
                    assertEquals(1, habit2Logs.size)
                    assertEquals(8, habit2Logs[0].streakDuration)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

    private fun createTestHabitLog(
        logId: Long = 0,
        habitId: Long,
        streakDuration: Int = 1,
        trigger: String? = null,
        notes: String? = null,
        createdAt: Date = Date(),
        updatedAt: Date = Date(),
    ) = HabitLog(
        logId = logId,
        habitId = habitId,
        streakDuration = streakDuration,
        trigger = trigger,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
