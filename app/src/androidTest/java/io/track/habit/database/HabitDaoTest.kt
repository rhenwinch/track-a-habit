package io.track.habit.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.track.habit.data.local.database.AppDatabase
import io.track.habit.data.local.database.dao.HabitDao
import io.track.habit.data.local.database.entities.Habit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.io.IOException
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HabitDaoTest {
    private lateinit var habitDao: HabitDao
    private lateinit var db: AppDatabase
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(
                    context = context,
                    klass = AppDatabase::class.java,
                ).build()
        habitDao = db.habitDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetHabit() =
        runTest(testDispatcher) {
            val habit = Habit(name = "Drink water")

            habitDao.insertHabit(habit)
            val allHabits = habitDao.getAllHabitsSortedByNameAsc().first()
            expectThat(allHabits) {
                get { size }.isEqualTo(1)
                get { get(0).name }.isEqualTo("Drink water")
            }
        }

    @Test
    @Throws(Exception::class)
    fun updateHabit() =
        runTest(testDispatcher) {
            val habit = Habit(name = "Exercise")
            val habitId = habitDao.insertHabit(habit)

            val updatedHabit =
                habit.copy(
                    habitId = habitId,
                    name = "Go for a run",
                )
            habitDao.updateHabit(updatedHabit)

            val allHabits = habitDao.getAllHabitsSortedByNameAsc().first()
            expectThat(allHabits) {
                get { size }.isEqualTo(1)
                get { get(0).name }.isEqualTo("Go for a run")
            }
        }

    @Test
    @Throws(Exception::class)
    fun deleteHabit() =
        runTest(testDispatcher) {
            val habit1 = Habit(name = "Read")
            val habit2 = Habit(name = "Meditate")
            val habitId1 = habitDao.insertHabit(habit1)
            habitDao.insertHabit(habit2)

            val retrievedHabit1 = habitDao.getHabitById(habitId1)

            retrievedHabit1?.let { habitToDelete ->
                habitDao.deleteHabit(habitToDelete)
            }

            val allHabits = habitDao.getAllHabitsSortedByNameAsc().first()
            expectThat(allHabits) {
                get { size }.isEqualTo(1)
                get { get(0).name }.isEqualTo("Meditate")
            }
        }

    @Test
    @Throws(Exception::class)
    fun getAllHabitsCountCheck() =
        runTest(testDispatcher) {
            val habit1 = Habit(name = "Learn Kotlin")
            val habit2 = Habit(name = "Code review")
            habitDao.insertHabit(habit1)
            habitDao.insertHabit(habit2)

            val allHabits = habitDao.getAllHabitsSortedByNameAsc().first()
            expectThat(allHabits) {
                get { size }.isEqualTo(2)
            }
        }

    // New sorting tests

    @Test
    @Throws(Exception::class)
    fun getAllHabitsSortedByNameAsc() =
        runTest(testDispatcher) {
            val habit1 = Habit(name = "Zebra habit")
            val habit2 = Habit(name = "Apple habit")
            val habit3 = Habit(name = "Monkey habit")

            habitDao.insertHabit(habit1)
            habitDao.insertHabit(habit2)
            habitDao.insertHabit(habit3)

            val sortedHabits = habitDao.getAllHabitsSortedByNameAsc().first()
            expectThat(sortedHabits) {
                get { size }.isEqualTo(3)
                get { get(0).name }.isEqualTo("Apple habit")
                get { get(1).name }.isEqualTo("Monkey habit")
                get { get(2).name }.isEqualTo("Zebra habit")
            }
        }

    @Test
    @Throws(Exception::class)
    fun getAllHabitsSortedByNameDesc() =
        runTest(testDispatcher) {
            val habit1 = Habit(name = "Apple habit")
            val habit2 = Habit(name = "Zebra habit")
            val habit3 = Habit(name = "Monkey habit")

            habitDao.insertHabit(habit1)
            habitDao.insertHabit(habit2)
            habitDao.insertHabit(habit3)

            val sortedHabits = habitDao.getAllHabitsSortedByNameDesc().first()
            expectThat(sortedHabits) {
                get { size }.isEqualTo(3)
                get { get(0).name }.isEqualTo("Zebra habit")
                get { get(1).name }.isEqualTo("Monkey habit")
                get { get(2).name }.isEqualTo("Apple habit")
            }
        }

    @Test
    @Throws(Exception::class)
    fun getAllHabitsSortedByCreationDateAsc() =
        runTest(testDispatcher) {
            val baseTime = System.currentTimeMillis()
            val oldDate = Date(baseTime - 86400000) // 1 day ago
            val midDate = Date(baseTime - 43200000) // 12 hours ago
            val newDate = Date(baseTime) // now

            val habit1 = Habit(name = "Old habit", createdAt = oldDate)
            val habit2 = Habit(name = "New habit", createdAt = newDate)
            val habit3 = Habit(name = "Mid habit", createdAt = midDate)

            habitDao.insertHabit(habit1)
            habitDao.insertHabit(habit2)
            habitDao.insertHabit(habit3)

            val sortedHabits = habitDao.getAllHabitsSortedByCreationDateAsc().first()
            expectThat(sortedHabits) {
                get { size }.isEqualTo(3)
                get { get(0).name }.isEqualTo("Old habit")
                get { get(1).name }.isEqualTo("Mid habit")
                get { get(2).name }.isEqualTo("New habit")
            }
        }

    @Test
    @Throws(Exception::class)
    fun getAllHabitsSortedByCreationDateDesc() =
        runTest(testDispatcher) {
            val baseTime = System.currentTimeMillis()
            val oldDate = Date(baseTime - 86400000) // 1 day ago
            val midDate = Date(baseTime - 43200000) // 12 hours ago
            val newDate = Date(baseTime) // now

            val habit1 = Habit(name = "Old habit", createdAt = oldDate)
            val habit2 = Habit(name = "New habit", createdAt = newDate)
            val habit3 = Habit(name = "Mid habit", createdAt = midDate)

            habitDao.insertHabit(habit1)
            habitDao.insertHabit(habit2)
            habitDao.insertHabit(habit3)

            val sortedHabits = habitDao.getAllHabitsSortedByCreationDateDesc().first()
            expectThat(sortedHabits) {
                get { size }.isEqualTo(3)
                get { get(0).name }.isEqualTo("New habit")
                get { get(1).name }.isEqualTo("Mid habit")
                get { get(2).name }.isEqualTo("Old habit")
            }
        }

    @Test
    @Throws(Exception::class)
    fun getHabitById() =
        runTest(testDispatcher) {
            val habit = Habit(name = "Test habit")
            val habitId = habitDao.insertHabit(habit)

            val retrievedHabit = habitDao.getHabitById(habitId)
            expectThat(retrievedHabit) {
                isNotNull()
                get { this!!.name }.isEqualTo("Test habit")
                get { habitId }.isEqualTo(habitId)
            }
        }

    @Test
    @Throws(Exception::class)
    fun getHabitByIdNotFound() =
        runTest(testDispatcher) {
            val retrievedHabit = habitDao.getHabitById(999L)
            expectThat(retrievedHabit).isEqualTo(null)
        }

    @Test
    @Throws(Exception::class)
    fun getHabitByIdFlow() =
        runTest(testDispatcher) {
            val habit = Habit(name = "Flow test habit")
            val habitId = habitDao.insertHabit(habit)

            val retrievedHabit = habitDao.getHabitByIdFlow(habitId).first()
            expectThat(retrievedHabit) {
                isNotNull()
                get { this!!.name }.isEqualTo("Flow test habit")
                get { habitId }.isEqualTo(habitId)
            }
        }

    @Test
    @Throws(Exception::class)
    fun getHabitByIdFlowNotFound() =
        runTest(testDispatcher) {
            val retrievedHabit = habitDao.getHabitByIdFlow(999L).first()
            expectThat(retrievedHabit).isEqualTo(null)
        }

    @Test
    @Throws(Exception::class)
    fun streakCalculationTest() =
        runTest(testDispatcher) {
            val baseTime = System.currentTimeMillis()
            val threeDaysAgo = Date(baseTime - 259200000) // 3 days ago

            val habit = Habit(name = "Streak test", lastResetAt = threeDaysAgo)
            val habitId = habitDao.insertHabit(habit)

            val retrievedHabit = habitDao.getHabitById(habitId)
            // The streak should be approximately 3 days
            expectThat(retrievedHabit) {
                isNotNull()
                get { this!!.streakInDays }.isEqualTo(3)
            }
        }

    @Test
    @Throws(Exception::class)
    fun getLongestStreakInDaysTest() =
        runTest(testDispatcher) {
            val baseTime = System.currentTimeMillis()

            // Create habits with different streak durations
            val oneDayAgo = Date(baseTime - 86400000) // 1 day ago
            val threeDaysAgo = Date(baseTime - 259200000) // 3 days ago
            val fiveDaysAgo = Date(baseTime - 432000000) // 5 days ago

            val habit1 = Habit(name = "Short streak", lastResetAt = oneDayAgo)
            val habit2 = Habit(name = "Medium streak", lastResetAt = threeDaysAgo)
            val habit3 = Habit(name = "Long streak", lastResetAt = fiveDaysAgo)

            habitDao.insertHabit(habit1)
            habitDao.insertHabit(habit2)
            habitDao.insertHabit(habit3)

            val longestStreak = habitDao.getLongestStreakInDays().first()

            // The longest streak should be 5 days (from habit3)
            expectThat(longestStreak).isEqualTo(5)
        }
}
