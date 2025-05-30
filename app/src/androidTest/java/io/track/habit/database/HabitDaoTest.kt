package io.track.habit.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.track.habit.data.local.database.AppDatabase
import io.track.habit.data.local.database.dao.HabitDao
import io.track.habit.data.local.database.entities.Habit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Date

@RunWith(AndroidJUnit4::class)
class HabitDaoTest {
    private lateinit var habitDao: HabitDao
    private lateinit var db: AppDatabase

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
        runBlocking {
            val habit = Habit(name = "Drink water")

            habitDao.insertHabit(habit)
            val allHabits = habitDao.getAllHabitsSortedByNameAsc().first()
            assertEquals(allHabits.size, 1)
            assertEquals(allHabits[0].name, "Drink water")
        }

    @Test
    @Throws(Exception::class)
    fun updateHabit() =
        runBlocking {
            val habit = Habit(name = "Exercise")
            val habitId = habitDao.insertHabit(habit)

            val updatedHabit =
                habit.copy(
                    habitId = habitId,
                    name = "Go for a run",
                )
            habitDao.updateHabit(updatedHabit)

            val allHabits = habitDao.getAllHabitsSortedByNameAsc().first()
            assertEquals(allHabits.size, 1)
            assertEquals(allHabits[0].name, "Go for a run")
        }

    @Test
    @Throws(Exception::class)
    fun deleteHabit() =
        runBlocking {
            val habit1 = Habit(name = "Read")
            val habit2 = Habit(name = "Meditate")
            val habitId1 = habitDao.insertHabit(habit1)
            habitDao.insertHabit(habit2)

            val retrievedHabit1 = habitDao.getHabitById(habitId1)

            retrievedHabit1?.let { habitToDelete ->
                habitDao.deleteHabit(habitToDelete)
            }

            val allHabits = habitDao.getAllHabitsSortedByNameAsc().first()
            assertEquals(allHabits.size, 1)
            assertEquals(allHabits[0].name, "Meditate")
        }

    @Test
    @Throws(Exception::class)
    fun getAllHabitsCountCheck() =
        runBlocking {
            val habit1 = Habit(name = "Learn Kotlin")
            val habit2 = Habit(name = "Code review")
            habitDao.insertHabit(habit1)
            habitDao.insertHabit(habit2)

            val allHabits = habitDao.getAllHabitsSortedByNameAsc().first()
            assertEquals(allHabits.size, 2)
        }

    // New sorting tests

    @Test
    @Throws(Exception::class)
    fun getAllHabitsSortedByNameAsc() =
        runBlocking {
            val habit1 = Habit(name = "Zebra habit")
            val habit2 = Habit(name = "Apple habit")
            val habit3 = Habit(name = "Monkey habit")

            habitDao.insertHabit(habit1)
            habitDao.insertHabit(habit2)
            habitDao.insertHabit(habit3)

            val sortedHabits = habitDao.getAllHabitsSortedByNameAsc().first()
            assertEquals(sortedHabits.size, 3)
            assertEquals(sortedHabits[0].name, "Apple habit")
            assertEquals(sortedHabits[1].name, "Monkey habit")
            assertEquals(sortedHabits[2].name, "Zebra habit")
        }

    @Test
    @Throws(Exception::class)
    fun getAllHabitsSortedByNameDesc() =
        runBlocking {
            val habit1 = Habit(name = "Apple habit")
            val habit2 = Habit(name = "Zebra habit")
            val habit3 = Habit(name = "Monkey habit")

            habitDao.insertHabit(habit1)
            habitDao.insertHabit(habit2)
            habitDao.insertHabit(habit3)

            val sortedHabits = habitDao.getAllHabitsSortedByNameDesc().first()
            assertEquals(sortedHabits.size, 3)
            assertEquals(sortedHabits[0].name, "Zebra habit")
            assertEquals(sortedHabits[1].name, "Monkey habit")
            assertEquals(sortedHabits[2].name, "Apple habit")
        }

    @Test
    @Throws(Exception::class)
    fun getAllHabitsSortedByCreationDateAsc() =
        runBlocking {
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
            assertEquals(sortedHabits.size, 3)
            assertEquals(sortedHabits[0].name, "Old habit")
            assertEquals(sortedHabits[1].name, "Mid habit")
            assertEquals(sortedHabits[2].name, "New habit")
        }

    @Test
    @Throws(Exception::class)
    fun getAllHabitsSortedByCreationDateDesc() =
        runBlocking {
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
            assertEquals(sortedHabits.size, 3)
            assertEquals(sortedHabits[0].name, "New habit")
            assertEquals(sortedHabits[1].name, "Mid habit")
            assertEquals(sortedHabits[2].name, "Old habit")
        }

    @Test
    @Throws(Exception::class)
    fun getHabitById() =
        runBlocking {
            val habit = Habit(name = "Test habit")
            val habitId = habitDao.insertHabit(habit)

            val retrievedHabit = habitDao.getHabitById(habitId)
            assertEquals(retrievedHabit?.name, "Test habit")
            assertEquals(retrievedHabit?.habitId, habitId)
        }

    @Test
    @Throws(Exception::class)
    fun getHabitByIdNotFound() =
        runBlocking {
            val retrievedHabit = habitDao.getHabitById(999L)
            assertEquals(retrievedHabit, null)
        }

    @Test
    @Throws(Exception::class)
    fun getHabitByIdFlow() =
        runBlocking {
            val habit = Habit(name = "Flow test habit")
            val habitId = habitDao.insertHabit(habit)

            val retrievedHabit = habitDao.getHabitByIdFlow(habitId).first()
            assertEquals(retrievedHabit?.name, "Flow test habit")
            assertEquals(retrievedHabit?.habitId, habitId)
        }

    @Test
    @Throws(Exception::class)
    fun getHabitByIdFlowNotFound() =
        runBlocking {
            val retrievedHabit = habitDao.getHabitByIdFlow(999L).first()
            assertEquals(retrievedHabit, null)
        }

    @Test
    @Throws(Exception::class)
    fun streakCalculationTest() =
        runBlocking {
            val baseTime = System.currentTimeMillis()
            val threeDaysAgo = Date(baseTime - 259200000) // 3 days ago

            val habit = Habit(name = "Streak test", lastResetAt = threeDaysAgo)
            val habitId = habitDao.insertHabit(habit)

            val retrievedHabit = habitDao.getHabitById(habitId)
            // The streak should be approximately 3 days
            assertEquals(retrievedHabit?.streakInDays, 3)
        }
}
