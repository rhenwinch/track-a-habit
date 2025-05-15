package io.track.habit.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.track.habit.data.database.dao.HabitDao
import io.track.habit.domain.model.database.Habit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class HabitDaoTest {

    private lateinit var habitDao: HabitDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context = context,
            klass = AppDatabase::class.java
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
    fun insertAndGetHabit() = runBlocking {
        val habit = Habit(name = "Drink water")

        habitDao.insertHabit(habit)
        val allHabits = habitDao.getAllHabits().first()
        assertEquals(allHabits.size, 1)
        assertEquals(allHabits[0].name, "Drink water")
    }

    @Test
    @Throws(Exception::class)
    fun updateHabit() = runBlocking {
        val habit = Habit(name = "Exercise")
        val habitId = habitDao.insertHabit(habit)

        val updatedHabit = habit.copy(
            habitId = habitId,
            name = "Go for a run"
        )
        habitDao.updateHabit(updatedHabit)

        val allHabits = habitDao.getAllHabits().first()
        assertEquals(allHabits.size, 1)
        assertEquals(allHabits[0].name, "Go for a run")
    }

    @Test
    @Throws(Exception::class)
    fun deleteHabit() = runBlocking {
        val habit1 = Habit(name = "Read")
        val habit2 = Habit(name = "Meditate")
        val habitId1 = habitDao.insertHabit(habit1)
        habitDao.insertHabit(habit2)

        val retrievedHabit1 = habitDao.getHabitById(habitId1)

        retrievedHabit1?.let { habitToDelete ->
            habitDao.deleteHabit(habitToDelete)
        }

        val allHabits = habitDao.getAllHabits().first()
        assertEquals(allHabits.size, 1)
        assertEquals(allHabits[0].name, "Meditate")
    }

    @Test
    @Throws(Exception::class)
    fun getAllHabits() = runBlocking {
        val habit1 = Habit(name = "Learn Kotlin")
        val habit2 = Habit(name = "Code review")
        habitDao.insertHabit(habit1)
        habitDao.insertHabit(habit2)

        val allHabits = habitDao.getAllHabits().first()
        assertEquals(allHabits.size, 2)
    }

    @Test
    @Throws(Exception::class)
    fun getActiveHabits() = runBlocking {
        val habit1 = Habit(name = "Walk", isActive = true)
        val habit2 = Habit(name = "Stretch", isActive = false)
        habitDao.insertHabit(habit1)
        habitDao.insertHabit(habit2)

        val activeHabits = habitDao.getActiveHabits().first()
        assertEquals(activeHabits.size, 1)
        assertEquals(activeHabits[0].name, "Walk")
    }

    @Test
    @Throws(Exception::class)
    fun setHabitInactive() = runBlocking {
        val habit = Habit(name = "Write journal", isActive = true)
        val habitId = habitDao.insertHabit(habit)
        habitDao.setHabitInactive(habitId)

        val allHabits = habitDao.getAllHabits().first()
        assertEquals(allHabits.size, 1)
        assertFalse(allHabits[0].isActive)
    }
}
