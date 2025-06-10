package io.track.habit.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import io.track.habit.data.local.database.AppDatabase
import io.track.habit.data.local.database.dao.HabitDao
import io.track.habit.data.local.database.dao.HabitLogDao
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.database.entities.HabitLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.io.IOException
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HabitLogDaoTest {
    private lateinit var habitLogDao: HabitLogDao
    private lateinit var habitDao: HabitDao
    private lateinit var db: AppDatabase
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .build()
        habitLogDao = db.habitLogDao()
        habitDao = db.habitDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertingAHabitLogAllowsRetrievingItById() =
        runTest(testDispatcher) {
            // Create a habit first to satisfy the foreign key constraint
            val habit = Habit(name = "Reading")
            val habitId = habitDao.insertHabit(habit)

            val habitLog =
                HabitLog(
                    habitId = habitId,
                    streakDuration = 5,
                    createdAt = Date(),
                    updatedAt = Date(),
                    trigger = "Evening",
                    notes = "Had a great day",
                )
            habitLogDao.insertHabitLog(habitLog)

            habitLogDao.getHabitLogsByHabitId(habitId).test {
                val logsForHabit = awaitItem()
                expectThat(logsForHabit) {
                    hasSize(1)
                    get { first() }.and {
                        get { habitId }.isEqualTo(habitId)
                        get { streakDuration }.isEqualTo(5)
                        get { trigger }.isEqualTo("Evening")
                        get { notes }.isEqualTo("Had a great day")
                    }
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun getHabitLogsByHabitIdReturnsAllLogsForASpecificHabit() =
        runTest(testDispatcher) {
            // Create habits first to satisfy the foreign key constraint
            val habit1 = Habit(name = "Habit 1")
            val habit2 = Habit(name = "Habit 2")
            val habitId1 = habitDao.insertHabit(habit1)
            val habitId2 = habitDao.insertHabit(habit2)

            val habitLog1 = HabitLog(habitId = habitId1, streakDuration = 1)
            val habitLog2 = HabitLog(habitId = habitId1, streakDuration = 2)
            val habitLog3 = HabitLog(habitId = habitId2, streakDuration = 3)

            habitLogDao.insertHabitLog(habitLog1)
            habitLogDao.insertHabitLog(habitLog2)
            habitLogDao.insertHabitLog(habitLog3)

            habitLogDao.getHabitLogsByHabitId(habitId1).test {
                val logsForHabit1 = awaitItem()
                expectThat(logsForHabit1).hasSize(2)
                cancelAndIgnoreRemainingEvents()
            }

            habitLogDao.getHabitLogsByHabitId(habitId2).test {
                val logsForHabit2 = awaitItem()
                expectThat(logsForHabit2).hasSize(1)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun updateHabitLogUpdatesTheCorrespondingLogRecord() =
        runTest(testDispatcher) {
            // Create a habit first to satisfy the foreign key constraint
            val habit = Habit(name = "Habit to Update")
            val habitId = habitDao.insertHabit(habit)

            val initialLog = HabitLog(habitId = habitId, streakDuration = 5, notes = "Initial notes")
            habitLogDao.insertHabitLog(initialLog)

            habitLogDao.getHabitLogsByHabitId(habitId).test {
                val logsForHabit = awaitItem()
                expectThat(logsForHabit).hasSize(1)
                val retrievedLog = logsForHabit.first()

                val updatedLog = retrievedLog.copy(streakDuration = 7, notes = "Updated notes", updatedAt = Date())
                habitLogDao.updateHabitLog(updatedLog)

                cancelAndIgnoreRemainingEvents()
            }

            habitLogDao.getHabitLogsByHabitId(habitId).test {
                val logsAfterUpdate = awaitItem()
                expectThat(logsAfterUpdate).hasSize(1)
                expectThat(logsAfterUpdate.first()) {
                    get { streakDuration }.isEqualTo(7)
                    get { notes }.isEqualTo("Updated notes")
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun getHabitLogByIdReturnsCorrectLogForGivenId() =
        runTest(testDispatcher) {
            // Create a habit first to satisfy the foreign key constraint
            val habit = Habit(name = "Specific Habit")
            val habitId = habitDao.insertHabit(habit)

            val habitLog = HabitLog(habitId = habitId, streakDuration = 15, notes = "Specific log")
            val logId = habitLogDao.insertHabitLog(habitLog)

            val retrievedLog = habitLogDao.getHabitLogById(logId)

            expectThat(retrievedLog) {
                isNotNull()
                get { this!!.logId }.isEqualTo(logId)
                get { this!!.habitId }.isEqualTo(habitId)
                get { this!!.streakDuration }.isEqualTo(15)
                get { this!!.notes }.isEqualTo("Specific log")
            }
        }

    @Test
    fun getLongestStreakForHabitReturnsLogWithHighestStreakDuration() =
        runTest(testDispatcher) {
            // Create a habit first to satisfy the foreign key constraint
            val habit = Habit(name = "Longest Streak Habit")
            val habitId = habitDao.insertHabit(habit)

            for (i in 1..5) {
                habitLogDao.insertHabitLog(HabitLog(habitId = habitId, streakDuration = i * 10))
            }

            habitLogDao.getLongestStreakForHabit(habitId).test {
                val longestStreakHabit = awaitItem()
                expectThat(longestStreakHabit) {
                    isNotNull()
                    get { this!!.habitId }.isEqualTo(habitId)
                    get { this!!.streakDuration }.isEqualTo(50)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun getLongestStreakAchievedReturnsHighestStreakDurationAcrossAllHabits() =
        runTest(testDispatcher) {
            // Create habits first to satisfy the foreign key constraint
            val habit1 = Habit(name = "Habit A")
            val habit2 = Habit(name = "Habit B")
            val habit3 = Habit(name = "Habit C")
            val habitId1 = habitDao.insertHabit(habit1)
            val habitId2 = habitDao.insertHabit(habit2)
            val habitId3 = habitDao.insertHabit(habit3)

            habitLogDao.insertHabitLog(HabitLog(habitId = habitId1, streakDuration = 15))
            habitLogDao.insertHabitLog(HabitLog(habitId = habitId1, streakDuration = 25))
            habitLogDao.insertHabitLog(HabitLog(habitId = habitId2, streakDuration = 10))
            habitLogDao.insertHabitLog(HabitLog(habitId = habitId2, streakDuration = 35))
            habitLogDao.insertHabitLog(HabitLog(habitId = habitId3, streakDuration = 5))

            habitLogDao.getLongestStreakAchieved().test {
                val habit = awaitItem()
                expectThat(habit).isEqualTo(35)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun getHabitLogsReturnsAllLogsOrderedByStreakDurationDesc() =
        runTest(testDispatcher) {
            // Create habits first to satisfy the foreign key constraint
            val habit1 = Habit(name = "Habit X")
            val habit2 = Habit(name = "Habit Y")
            val habit3 = Habit(name = "Habit Z")
            val habitId1 = habitDao.insertHabit(habit1)
            val habitId2 = habitDao.insertHabit(habit2)
            val habitId3 = habitDao.insertHabit(habit3)

            habitLogDao.insertHabitLog(HabitLog(habitId = habitId1, streakDuration = 15, notes = "Log 1"))
            habitLogDao.insertHabitLog(HabitLog(habitId = habitId2, streakDuration = 30, notes = "Log 2"))
            habitLogDao.insertHabitLog(HabitLog(habitId = habitId1, streakDuration = 5, notes = "Log 3"))
            habitLogDao.insertHabitLog(HabitLog(habitId = habitId3, streakDuration = 25, notes = "Log 4"))
            habitLogDao.insertHabitLog(HabitLog(habitId = habitId2, streakDuration = 20, notes = "Log 5"))

            habitLogDao.getHabitLogs().test {
                val allLogs = awaitItem()

                expectThat(allLogs).hasSize(5)

                expectThat(allLogs[0].streakDuration).isEqualTo(30)
                expectThat(allLogs[1].streakDuration).isEqualTo(25)
                expectThat(allLogs[2].streakDuration).isEqualTo(20)
                expectThat(allLogs[3].streakDuration).isEqualTo(15)
                expectThat(allLogs[4].streakDuration).isEqualTo(5)

                expectThat(allLogs[0].notes).isEqualTo("Log 2")
                expectThat(allLogs[1].notes).isEqualTo("Log 4")
                expectThat(allLogs[2].notes).isEqualTo("Log 5")
                expectThat(allLogs[3].notes).isEqualTo("Log 1")
                expectThat(allLogs[4].notes).isEqualTo("Log 3")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun deletingAHabitCascadesAndDeletesAllAssociatedHabitLogs() =
        runTest(testDispatcher) {
            val habit = Habit(name = "Read books")
            val habitId = habitDao.insertHabit(habit)

            val log1 = HabitLog(habitId = habitId, streakDuration = 5, notes = "First streak")
            val log2 = HabitLog(habitId = habitId, streakDuration = 10, notes = "Second streak")
            val log3 = HabitLog(habitId = habitId, streakDuration = 15, notes = "Third streak")

            habitLogDao.insertHabitLog(log1)
            habitLogDao.insertHabitLog(log2)
            habitLogDao.insertHabitLog(log3)

            turbineScope {
                val habitLogsFlow = habitLogDao.getHabitLogsByHabitId(habitId).testIn(backgroundScope)

                expectThat(habitLogsFlow.awaitItem()).hasSize(3)

                habitDao.deleteHabit(habit.copy(habitId = habitId))

                expectThat(habitLogsFlow.awaitItem()).isEmpty()

                habitLogsFlow.cancelAndIgnoreRemainingEvents()
            }
        }
}
