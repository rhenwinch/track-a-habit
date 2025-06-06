package io.track.habit.repository

import android.database.sqlite.SQLiteConstraintException
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.utils.SortOrder
import io.track.habit.repository.fake.FakeHabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.Date

class HabitRepositoryTest {
    private lateinit var repository: HabitRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        repository = FakeHabitRepository()
    }

    @Test
    fun `insert habit should add habit to repository and return positive id`() =
        runTest {
            val habitName = "Read a book"
            val newHabit = Habit(name = habitName)
            val habitId = repository.insertHabit(newHabit)

            assertTrue("Returned ID should be positive", habitId > 0L)
            val retrievedHabit = repository.getHabitById(habitId)
            assertNotNull("Retrieved habit should not be null", retrievedHabit)
            assertEquals("Inserted and retrieved habit names should match", habitName, retrievedHabit?.name)
        }

    @Test
    fun `insert habit should fail when duplicate name is provided`() =
        runTest {
            val habitName = "Unique Name"
            val habit1 = Habit(name = habitName, createdAt = Date())
            val id1 = repository.insertHabit(habit1)
            assertTrue(id1 > 0L)

            val habit2 = Habit(name = habitName, createdAt = Date()) // Same name

            var caughtException: SQLiteConstraintException? = null

            try {
                repository.insertHabit(habit2)
                fail("Expected SQLiteConstraintException to be thrown, but it wasn't.")
            } catch (e: SQLiteConstraintException) {
                caughtException = e
            } catch (e: Exception) {
                fail("Expected SQLiteConstraintException, but caught ${e::class.simpleName}: ${e.message}")
            }

            assertNotNull("SQLiteConstraintException should have been caught.", caughtException)

            val habits = repository.getAllHabits().first()
            assertEquals(
                "Should only have one habit with the unique name after failed insert attempt",
                1,
                habits.count { it.name == habitName },
            )
        }

    @Test
    fun `delete habit should remove habit from repository`() =
        runTest {
            val habit = Habit(name = "Exercise")
            val habitId = repository.insertHabit(habit)

            val habitToDelete = repository.getHabitById(habitId)!!
            repository.deleteHabit(habitToDelete)

            val retrievedHabit = repository.getHabitById(habitId)
            assertNull("Deleted habit should not be found", retrievedHabit)
        }

    @Test
    fun `delete habit should not affect other habits when non-existent habit is deleted`() =
        runTest {
            val existingHabit = Habit(name = "Meditate")
            val existingHabitId = repository.insertHabit(existingHabit)

            // Create a Habit object that doesn't match any in the repository
            val nonExistentHabit = Habit(habitId = 999L, name = "Non Existent")
            repository.deleteHabit(nonExistentHabit) // Should do nothing if no match

            val retrievedExistingHabit = repository.getHabitById(existingHabitId)
            assertNotNull("Existing habit should still be present", retrievedExistingHabit)
            val allHabits = repository.getAllHabits().first()
            assertEquals("Only one habit should remain", 1, allHabits.size)
        }

    @Test
    fun `update habit should not create new habit when non-existent habit is updated`() =
        runTest {
            val existingHabit = Habit(name = "Journaling")
            repository.insertHabit(existingHabit)
            val initialCount = repository.getAllHabits().first().size

            val nonExistentHabit = Habit(habitId = 999L, name = "Ghost Habit")
            repository.updateHabit(nonExistentHabit) // Should do nothing if no match

            assertEquals(
                "Number of habits should remain the same",
                initialCount,
                repository.getAllHabits().first().size,
            )
            assertNull("Non-existent habit should not have been created", repository.getHabitById(999L))
        }

    @Test
    fun `get all habits should return empty list when no habits exist`() =
        runTest {
            repository.getAllHabits().test {
                assertTrue("Should emit an empty list initially", expectMostRecentItem().isEmpty())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should return all inserted habits`() =
        runTest {
            val habit1 = Habit(name = "Yoga")
            val habit2 = Habit(name = "Coding practice")
            repository.insertHabit(habit1)
            repository.insertHabit(habit2)

            repository.getAllHabits().test {
                val list = expectMostRecentItem()
                assertEquals("Should retrieve all inserted habits", 2, list.size)
                assertTrue("List should contain habit1", list.any { it.name == habit1.name })
                assertTrue("List should contain habit2", list.any { it.name == habit2.name })
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should emit updates when habits are added`() =
        runTest {
            repository.getAllHabits().test {
                assertTrue("Initial list should be empty", awaitItem().isEmpty())

                val habit1 = Habit(name = "New Habit 1")
                val id1 = repository.insertHabit(habit1)

                val listAfterInsert1 = expectMostRecentItem()
                assertEquals("List should contain 1 habit", 1, listAfterInsert1.size)
                assertEquals(id1, listAfterInsert1.first().habitId)

                val habit2 = Habit(name = "New Habit 2")
                val id2 = repository.insertHabit(habit2)

                val listAfterInsert2 = awaitItem()
                assertEquals("List should contain 2 habits", 2, listAfterInsert2.size)
                assertTrue(listAfterInsert2.any { it.habitId == id2 })

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should emit updates when habits are deleted`() =
        runTest {
            val habit1 = Habit(name = "To Delete")
            val habit2 = Habit(name = "To Keep")
            val id1 = repository.insertHabit(habit1)
            val id2 = repository.insertHabit(habit2)

            repository.getAllHabits().test {
                assertEquals("Initial list should have 2 habits", 2, expectMostRecentItem().size)

                val habitToDelete = repository.getHabitById(id1)!!
                repository.deleteHabit(habitToDelete)

                val listAfterDelete = awaitItem()
                assertEquals("List should have 1 habit after delete", 1, listAfterDelete.size)
                assertEquals(id2, listAfterDelete.first().habitId)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should emit updates when habits are updated`() =
        runTest {
            val initialName = "Original Name"
            val updatedName = "Updated Name"
            val habit = Habit(name = initialName)
            val habitId = repository.insertHabit(habit)

            repository.getAllHabits().test {
                var currentList = expectMostRecentItem()
                assertEquals(initialName, currentList.first { it.habitId == habitId }.name)

                val habitToUpdate = repository.getHabitById(habitId)!!.copy(name = updatedName)
                repository.updateHabit(habitToUpdate)

                currentList = awaitItem()
                assertEquals(updatedName, currentList.first { it.habitId == habitId }.name)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should be sorted by name`() =
        runTest {
            val habit1 = Habit(habitId = 1, name = "Z")
            val habit2 = Habit(habitId = 2, name = "A")
            repository.insertHabit(habit1)
            repository.insertHabit(habit2)

            turbineScope {
                val ascendingTurbine = repository.getAllHabits(SortOrder.Name()).testIn(backgroundScope)
                val descendingTurbine =
                    repository.getAllHabits(SortOrder.Name(ascending = false)).testIn(backgroundScope)

                with(ascendingTurbine) {
                    val list = expectMostRecentItem()
                    assertEquals("Should be sorted by name in an ascending order", listOf(habit2, habit1), list)
                }

                ascendingTurbine.cancelAndConsumeRemainingEvents()

                with(descendingTurbine) {
                    val list = awaitItem()
                    assertEquals("Should be sorted by name in a descending order", listOf(habit1, habit2), list)
                    cancelAndConsumeRemainingEvents()
                }
            }
        }

    @Test
    fun `get all habits should be sorted by creation date`() =
        runTest {
            val habit1 = Habit(habitId = 1, name = "Z")
            val habit2 = Habit(habitId = 2, name = "A", createdAt = Date().apply { time -= 1000 })
            repository.insertHabit(habit1)
            repository.insertHabit(habit2)

            turbineScope {
                val ascendingTurbine = repository.getAllHabits(SortOrder.Creation()).testIn(backgroundScope)
                val descendingTurbine =
                    repository.getAllHabits(SortOrder.Creation(ascending = false)).testIn(backgroundScope)

                with(ascendingTurbine) {
                    val list = expectMostRecentItem()
                    assertEquals("Should be sorted by name in an ascending order", listOf(habit2, habit1), list)
                    cancelAndConsumeRemainingEvents()
                }

                with(descendingTurbine) {
                    val list = expectMostRecentItem()
                    assertEquals("Should be sorted by name in a descending order", listOf(habit1, habit2), list)
                    cancelAndConsumeRemainingEvents()
                }
            }
        }

    @Test
    fun `get all habits should be sorted by streak`() =
        runTest {
            val habit1 = Habit(habitId = 1, name = "Z")
            val habit2 = Habit(habitId = 2, name = "A", lastResetAt = Date().apply { time -= 5 * 1000 * 60 * 60 * 24 })
            repository.insertHabit(habit1)
            repository.insertHabit(habit2)

            turbineScope {
                val ascendingTurbine = repository.getAllHabits(SortOrder.Streak()).testIn(backgroundScope)
                val descendingTurbine =
                    repository.getAllHabits(SortOrder.Streak(ascending = false)).testIn(backgroundScope)

                with(ascendingTurbine) {
                    val list = expectMostRecentItem()
                    assertEquals("Should be sorted by streak in an ascending order", listOf(habit1, habit2), list)
                    cancelAndConsumeRemainingEvents()
                }

                with(descendingTurbine) {
                    val list = expectMostRecentItem()
                    assertEquals("Should be sorted by streak in a descending order", listOf(habit2, habit1), list)
                    cancelAndConsumeRemainingEvents()
                }
            }
        }

    @Test
    fun `get habit by id should return habit when valid id is provided`() =
        runTest {
            val habitName = "Clean room"
            val habit = Habit(name = habitName)
            val habitId = repository.insertHabit(habit)

            val retrievedHabit = repository.getHabitById(habitId)
            assertNotNull(retrievedHabit)
            assertEquals(habitName, retrievedHabit?.name)
            assertEquals(habitId, retrievedHabit?.habitId)
        }

    @Test
    fun `get habit by id should return null when non-existent id is provided`() =
        runTest {
            val retrievedHabit = repository.getHabitById(12345L) // Non-existent ID
            assertNull("Should return null for non-existent ID", retrievedHabit)
        }

    @Test
    fun `get habit by id should return null when invalid id is provided`() =
        runTest {
            // Assuming standard autoGenerate = true, which starts from 1.
            var retrievedHabit = repository.getHabitById(0L)
            assertNull("Should return null for ID 0", retrievedHabit)

            retrievedHabit = repository.getHabitById(-10L)
            assertNull("Should return null for negative ID", retrievedHabit)
        }

    @Test
    fun `get habit by id flow should emit habit when valid id is provided`() =
        runTest {
            val habitName = "Walk dog"
            val habit = Habit(name = habitName)
            val habitId = repository.insertHabit(habit)

            repository.getHabitByIdFlow(habitId).test {
                val emittedHabit = expectMostRecentItem()
                assertNotNull(emittedHabit)
                assertEquals(habitName, emittedHabit?.name)
                assertEquals(habitId, emittedHabit?.habitId)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get habit by id flow should return null when non-existent id is provided`() =
        runTest {
            repository.getHabitByIdFlow(54321L).test {
                assertNull("Flow should emit null for non-existent ID", awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get habit by id flow should emit updates when habit is updated`() =
        runTest {
            val initialName = "Initial Flow Name"
            val updatedName = "Updated Flow Name"
            val habit = Habit(name = initialName)
            val habitId = repository.insertHabit(habit)

            repository.getHabitByIdFlow(habitId).test {
                assertEquals(initialName, expectMostRecentItem()?.name)

                val habitToUpdate = repository.getHabitById(habitId)!!.copy(name = updatedName)
                repository.updateHabit(habitToUpdate)

                assertEquals(updatedName, awaitItem()?.name)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get habit by id flow should emit null after habit is deleted`() =
        runTest {
            val habit = Habit(name = "To be deleted via flow")
            val habitId = repository.insertHabit(habit)

            repository.getHabitByIdFlow(habitId).test {
                assertNotNull("Should emit habit initially", expectMostRecentItem())

                val habitToDelete = repository.getHabitById(habitId)!!
                repository.deleteHabit(habitToDelete)

                assertNull("Should emit null after deletion", awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }
}
