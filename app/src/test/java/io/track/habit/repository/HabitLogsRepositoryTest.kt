package io.track.habit.repository

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.repository.fake.FakeHabitLogsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
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
            expectThat(retrievedLog) {
                isNotNull()
                get { this!!.habitId }.isEqualTo(1L)
                get { this!!.streakDuration }.isEqualTo(5)
                get { this!!.trigger }.isEqualTo("Morning alarm")
                get { this!!.notes }.isEqualTo("Felt great today")
            }
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

            expectThat(result) {
                isNotNull()
                get { this!!.logId }.isEqualTo(1L)
                get { this!!.habitId }.isEqualTo(1L)
                get { this!!.streakDuration }.isEqualTo(3)
                get { this!!.trigger }.isEqualTo("Before bed")
                get { this!!.notes }.isEqualTo("Read 20 pages")
            }
        }

    @Test
    fun `when getting habit log by non-existent id then returns null`() =
        runTest {
            val result = repository.getHabitLogById(999L)

            expectThat(result).isNull()
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
            expectThat(result) {
                isNotNull()
                get { this!!.streakDuration }.isEqualTo(7)
                get { this!!.trigger }.isEqualTo("Morning routine")
                get { this!!.notes }.isEqualTo("10 minutes meditation")
            }
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
                expectThat(logs) {
                    hasSize(3)
                    get { this[0].streakDuration }.isEqualTo(3) // Most recent
                    get { this[1].streakDuration }.isEqualTo(2)
                    get { this[2].streakDuration }.isEqualTo(1) // Oldest
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when getting habit logs for habit with no logs then returns empty list`() =
        runTest {
            val habitId = 1L

            repository.getHabitLogsByHabitId(habitId).test {
                val logs = awaitItem()
                expectThat(logs).isEmpty()
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
                expectThat(longestStreak) {
                    isNotNull()
                    get { this!!.streakDuration }.isEqualTo(10)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when getting longest streak achieved then returns highest streak duration from all habits`() =
        runTest {
            // Insert logs with different streak durations for different habits
            val habit1Id = 1L
            val habit2Id = 2L

            repository.insertHabitLog(createTestHabitLog(habitId = habit1Id, streakDuration = 5))
            repository.insertHabitLog(createTestHabitLog(habitId = habit1Id, streakDuration = 12))
            repository.insertHabitLog(createTestHabitLog(habitId = habit2Id, streakDuration = 8))
            repository.insertHabitLog(createTestHabitLog(habitId = habit2Id, streakDuration = 20)) // Highest overall

            repository.getLongestStreakInDays().test {
                val longestStreak = expectMostRecentItem()
                expectThat(longestStreak).isEqualTo(20)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when no habit logs exist then longest streak achieved returns zero`() =
        runTest {
            repository.getLongestStreakInDays().test {
                val longestStreak = expectMostRecentItem()
                expectThat(longestStreak).isEqualTo(0)
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

                expectThat(result) {
                    isNotNull()
                    get { this!!.habit.name }.isEqualTo("Test Habit")
                    get { this!!.logs }.hasSize(2)
                    get { this!!.logs[0].notes }.isEqualTo("Evening reflection")
                    get { this!!.logs[1].notes }.isEqualTo("Morning pages")
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when getting habit with logs for existing habit with no logs then returns habit with empty logs list`() =
        runTest {
            val habitId = 1L

            repository.getHabitWithLogs(habitId).test {
                val result = awaitItem()

                expectThat(result) {
                    isNotNull()
                    get { this!!.habit.habitId }.isEqualTo(habitId)
                    get { this!!.habit.name }.isEqualTo("Test Habit")
                    get { this!!.logs }.isEmpty()
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when getting habit with logs for non-existent habit then returns null`() =
        runTest {
            repository.getHabitWithLogs(999L).test {
                val result = awaitItem()
                expectThat(result).isNull()
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
                expectThat(logsAfterFirst).hasSize(1)
                expectThat(logsAfterFirst[0].streakDuration).isEqualTo(1)

                val log2 =
                    createTestHabitLog(
                        habitId = habitId,
                        streakDuration = 2,
                        createdAt = Date(System.currentTimeMillis() + 300),
                    )
                repository.insertHabitLog(log2)

                val logsAfterSecond = awaitItem()
                expectThat(logsAfterSecond) {
                    hasSize(2)
                    get { this[0].streakDuration }.isEqualTo(2)
                    get { this[1].streakDuration }.isEqualTo(1)
                }

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
                expectThat(longestAfterFirst?.streakDuration).isEqualTo(5)

                val log2 = createTestHabitLog(habitId = habitId, streakDuration = 3)
                repository.insertHabitLog(log2)

                val log3 = createTestHabitLog(habitId = habitId, streakDuration = 10)
                repository.insertHabitLog(log3)

                val longestAfterThird = expectMostRecentItem()
                expectThat(longestAfterThird?.streakDuration).isEqualTo(10)

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
                    expectThat(habit1Logs) {
                        hasSize(2)
                        get { this[0].streakDuration }.isEqualTo(3)
                        get { this[1].streakDuration }.isEqualTo(5)
                    }
                    cancelAndIgnoreRemainingEvents()
                }

                with(habit2Turbine) {
                    val habit2Logs = expectMostRecentItem()
                    expectThat(habit2Logs) {
                        hasSize(1)
                        get { this[0].streakDuration }.isEqualTo(8)
                    }
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

    @Test
    fun `when getting all habit logs then returns logs sorted by streak duration in descending order`() =
        runTest {
            // Insert logs with different streak durations
            val habit1Id = 1L
            val habit2Id = 2L

            repository.insertHabitLog(createTestHabitLog(habitId = habit1Id, streakDuration = 5))
            repository.insertHabitLog(createTestHabitLog(habitId = habit1Id, streakDuration = 12))
            repository.insertHabitLog(createTestHabitLog(habitId = habit2Id, streakDuration = 8))
            repository.insertHabitLog(createTestHabitLog(habitId = habit2Id, streakDuration = 3))

            repository.getHabitLogs().test {
                val logs = expectMostRecentItem()

                expectThat(logs) {
                    hasSize(4)
                    get { this[0].streakDuration }.isEqualTo(12) // Highest streak first
                    get { this[1].streakDuration }.isEqualTo(8)
                    get { this[2].streakDuration }.isEqualTo(5)
                    get { this[3].streakDuration }.isEqualTo(3) // Lowest streak last
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when no habit logs exist and getting all habit logs then returns empty list`() =
        runTest {
            repository.getHabitLogs().test {
                val logs = expectMostRecentItem()
                expectThat(logs).isEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when observing all habit logs flow and adding new logs then emits updated data`() =
        runTest {
            // Start observing the flow
            repository.getHabitLogs().test {
                // Initially should be empty
                val initialLogs = expectMostRecentItem()
                expectThat(initialLogs).isEmpty()

                // Add first log
                val habit1Id = 1L
                val log1 = createTestHabitLog(habitId = habit1Id, streakDuration = 5)
                repository.insertHabitLog(log1)

                val logsAfterFirst = awaitItem()
                expectThat(logsAfterFirst) {
                    hasSize(1)
                    get { this[0].streakDuration }.isEqualTo(5)
                }

                // Add second log with higher streak
                val log2 = createTestHabitLog(habitId = habit1Id, streakDuration = 10)
                repository.insertHabitLog(log2)

                val logsAfterSecond = awaitItem()
                expectThat(logsAfterSecond) {
                    hasSize(2)
                    get { this[0].streakDuration }.isEqualTo(10) // Should be first (highest)
                    get { this[1].streakDuration }.isEqualTo(5)
                }

                // Update first log to have highest streak
                val updatedLog =
                    createTestHabitLog(
                        logId = 1L,
                        habitId = habit1Id,
                        streakDuration = 15,
                    )
                repository.updateHabitLog(updatedLog)

                val logsAfterUpdate = awaitItem()
                expectThat(logsAfterUpdate) {
                    hasSize(2)
                    get { this[0].streakDuration }.isEqualTo(15) // Updated log should now be first
                    get { this[1].streakDuration }.isEqualTo(10)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun createTestHabitLog(
        logId: Long = 0,
        habitId: Long,
        streakDuration: Int,
        trigger: String? = null,
        notes: String? = null,
        createdAt: Date = Date(),
    ): HabitLog {
        return HabitLog(
            logId = logId,
            habitId = habitId,
            streakDuration = streakDuration,
            trigger = trigger,
            notes = notes,
            createdAt = createdAt,
            updatedAt = createdAt,
        )
    }
}
