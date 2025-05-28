package io.track.habit.repository

import android.database.sqlite.SQLiteConstraintException
import app.cash.turbine.test
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.repository.fake.FakeHabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class HabitRepositoryTest {
    private lateinit var repository: HabitRepository

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        repository = FakeHabitRepository()
    }

    @Test
    fun `insert habit should add habit to repository and return positive id`() =
        testScope.runTest {
            val habitName = "Read a book"
            val newHabit = Habit(name = habitName, isActive = true, createdAt = Date(), updatedAt = Date())
            val habitId = repository.insertHabit(newHabit)

            assertTrue("Returned ID should be positive", habitId > 0L)
            val retrievedHabit = repository.getHabitById(habitId)
            assertNotNull("Retrieved habit should not be null", retrievedHabit)
            assertEquals("Inserted and retrieved habit names should match", habitName, retrievedHabit?.name)
            assertTrue("Habit should be active as set", retrievedHabit?.isActive == true)
        }

    @Test
    fun `insert habit should fail when duplicate name is provided`() =
        testScope.runTest {
            val habitName = "Unique Name"
            val habit1 = Habit(name = habitName, createdAt = Date(), updatedAt = Date())
            val id1 = repository.insertHabit(habit1)
            assertTrue(id1 > 0L)

            val habit2 = Habit(name = habitName, createdAt = Date(), updatedAt = Date()) // Same name

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
        testScope.runTest {
            val habit = Habit(name = "Exercise", createdAt = Date(), updatedAt = Date())
            val habitId = repository.insertHabit(habit)

            val habitToDelete = repository.getHabitById(habitId)!!
            repository.deleteHabit(habitToDelete)

            val retrievedHabit = repository.getHabitById(habitId)
            assertNull("Deleted habit should not be found", retrievedHabit)
        }

    @Test
    fun `delete habit should not affect other habits when non-existent habit is deleted`() =
        testScope.runTest {
            val existingHabit = Habit(name = "Meditate", createdAt = Date(), updatedAt = Date())
            val existingHabitId = repository.insertHabit(existingHabit)

            // Create a Habit object that doesn't match any in the repository
            val nonExistentHabit = Habit(habitId = 999L, name = "Non Existent", createdAt = Date(), updatedAt = Date())
            repository.deleteHabit(nonExistentHabit) // Should do nothing if no match

            val retrievedExistingHabit = repository.getHabitById(existingHabitId)
            assertNotNull("Existing habit should still be present", retrievedExistingHabit)
            val allHabits = repository.getAllHabits().first()
            assertEquals("Only one habit should remain", 1, allHabits.size)
        }

    @Test
    fun `update habit should modify habit properties in repository`() =
        testScope.runTest {
            val initialName = "Drink water"
            val updatedName = "Drink 2L of water"
            val habit =
                Habit(
                    name = initialName,
                    isActive = true,
                )
            val habitId = repository.insertHabit(habit)

            val insertedHabit = repository.getHabitById(habitId)!!
            val habitToUpdate =
                insertedHabit.copy(
                    name = updatedName,
                    isActive = false,
                    updatedAt = Date(),
                    createdAt = insertedHabit.createdAt,
                )
            repository.updateHabit(habitToUpdate)

            val updatedHabit = repository.getHabitById(habitId)
            assertNotNull(updatedHabit)
            assertEquals("Habit name should be updated", updatedName, updatedHabit?.name)
            assertFalse("Habit isActive status should be updated", updatedHabit?.isActive != false)
            assertTrue("UpdatedAt should be more recent", updatedHabit!!.updatedAt.after(insertedHabit.createdAt))
        }

    @Test
    fun `update habit should not create new habit when non-existent habit is updated`() =
        testScope.runTest {
            val existingHabit = Habit(name = "Journaling", createdAt = Date(), updatedAt = Date())
            repository.insertHabit(existingHabit)
            val initialCount = repository.getAllHabits().first().size

            val nonExistentHabit = Habit(habitId = 999L, name = "Ghost Habit", createdAt = Date(), updatedAt = Date())
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
        testScope.runTest {
            repository.getAllHabits().test {
                assertTrue("Should emit an empty list initially", awaitItem().isEmpty())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should return all inserted habits`() =
        testScope.runTest {
            val habit1 = Habit(name = "Yoga", createdAt = Date(), updatedAt = Date())
            val habit2 = Habit(name = "Coding practice", createdAt = Date(), updatedAt = Date())
            repository.insertHabit(habit1)
            repository.insertHabit(habit2)

            repository.getAllHabits().test {
                val list = awaitItem()
                assertEquals("Should retrieve all inserted habits", 2, list.size)
                assertTrue("List should contain habit1", list.any { it.name == habit1.name })
                assertTrue("List should contain habit2", list.any { it.name == habit2.name })
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should emit updates when habits are added`() =
        testScope.runTest {
            repository.getAllHabits().test {
                assertTrue("Initial list should be empty", awaitItem().isEmpty())

                val habit1 = Habit(name = "New Habit 1", createdAt = Date(), updatedAt = Date())
                val id1 = repository.insertHabit(habit1)
                val listAfterInsert1 = awaitItem()
                assertEquals("List should contain 1 habit", 1, listAfterInsert1.size)
                assertEquals(id1, listAfterInsert1.first().habitId)

                val habit2 = Habit(name = "New Habit 2", createdAt = Date(), updatedAt = Date())
                val id2 = repository.insertHabit(habit2)
                val listAfterInsert2 = awaitItem()
                assertEquals("List should contain 2 habits", 2, listAfterInsert2.size)
                assertTrue(listAfterInsert2.any { it.habitId == id2 })

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should emit updates when habits are deleted`() =
        testScope.runTest {
            val habit1 = Habit(name = "To Delete", createdAt = Date(), updatedAt = Date())
            val habit2 = Habit(name = "To Keep", createdAt = Date(), updatedAt = Date())
            val id1 = repository.insertHabit(habit1)
            val id2 = repository.insertHabit(habit2)

            repository.getAllHabits().test {
                assertEquals("Initial list should have 2 habits", 2, awaitItem().size)

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
        testScope.runTest {
            val initialName = "Original Name"
            val updatedName = "Updated Name"
            val habit = Habit(name = initialName, createdAt = Date(), updatedAt = Date())
            val habitId = repository.insertHabit(habit)

            repository.getAllHabits().test {
                var currentList = awaitItem()
                assertEquals(initialName, currentList.first { it.habitId == habitId }.name)

                val habitToUpdate = repository.getHabitById(habitId)!!.copy(name = updatedName, updatedAt = Date())
                repository.updateHabit(habitToUpdate)

                currentList = awaitItem()
                assertEquals(updatedName, currentList.first { it.habitId == habitId }.name)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get habit by id should return habit when valid id is provided`() =
        testScope.runTest {
            val habitName = "Clean room"
            val habit = Habit(name = habitName, createdAt = Date(), updatedAt = Date())
            val habitId = repository.insertHabit(habit)

            val retrievedHabit = repository.getHabitById(habitId)
            assertNotNull(retrievedHabit)
            assertEquals(habitName, retrievedHabit?.name)
            assertEquals(habitId, retrievedHabit?.habitId)
        }

    @Test
    fun `get habit by id should return null when non-existent id is provided`() =
        testScope.runTest {
            val retrievedHabit = repository.getHabitById(12345L) // Non-existent ID
            assertNull("Should return null for non-existent ID", retrievedHabit)
        }

    @Test
    fun `get habit by id should return null when invalid id is provided`() =
        testScope.runTest {
            // Assuming standard autoGenerate = true, which starts from 1.
            var retrievedHabit = repository.getHabitById(0L)
            assertNull("Should return null for ID 0", retrievedHabit)

            retrievedHabit = repository.getHabitById(-10L)
            assertNull("Should return null for negative ID", retrievedHabit)
        }

    @Test
    fun `get habit by id flow should emit habit when valid id is provided`() =
        testScope.runTest {
            val habitName = "Walk dog"
            val habit = Habit(name = habitName, createdAt = Date(), updatedAt = Date())
            val habitId = repository.insertHabit(habit)

            repository.getHabitByIdFlow(habitId).test {
                val emittedHabit = awaitItem()
                assertNotNull(emittedHabit)
                assertEquals(habitName, emittedHabit?.name)
                assertEquals(habitId, emittedHabit?.habitId)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get habit by id flow should return null when non-existent id is provided`() =
        testScope.runTest {
            repository.getHabitByIdFlow(54321L).test {
                // Non-existent ID
                assertNull("Flow should emit null for non-existent ID", awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get habit by id flow should emit updates when habit is updated`() =
        testScope.runTest {
            val initialName = "Initial Flow Name"
            val updatedName = "Updated Flow Name"
            val habit = Habit(name = initialName, createdAt = Date(), updatedAt = Date())
            val habitId = repository.insertHabit(habit)

            repository.getHabitByIdFlow(habitId).test {
                assertEquals(initialName, awaitItem()?.name)

                val habitToUpdate = repository.getHabitById(habitId)!!.copy(name = updatedName, updatedAt = Date())
                repository.updateHabit(habitToUpdate)

                assertEquals(updatedName, awaitItem()?.name)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get habit by id flow should emit null after habit is deleted`() =
        testScope.runTest {
            val habit = Habit(name = "To be deleted via flow", createdAt = Date(), updatedAt = Date())
            val habitId = repository.insertHabit(habit)

            repository.getHabitByIdFlow(habitId).test {
                assertNotNull("Should emit habit initially", awaitItem())

                val habitToDelete = repository.getHabitById(habitId)!!
                repository.deleteHabit(habitToDelete)

                assertNull("Should emit null after deletion", awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get active habits should return only active habits`() =
        testScope.runTest {
            val activeHabit = Habit(name = "Active Habit", isActive = true, createdAt = Date(), updatedAt = Date())
            val inactiveHabit = Habit(name = "Inactive Habit", isActive = false, createdAt = Date(), updatedAt = Date())
            repository.insertHabit(activeHabit)
            repository.insertHabit(inactiveHabit)

            repository.getActiveHabits().test {
                val list = awaitItem()
                assertEquals("Should only retrieve active habits", 1, list.size)
                assertEquals(activeHabit.name, list[0].name)
                assertTrue(list[0].isActive)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get active habits should return empty list when no active habits exist`() =
        testScope.runTest {
            // Scenario 1: No habits exist
            repository.getActiveHabits().test {
                assertTrue("Should be empty if no habits", awaitItem().isEmpty())

                // Scenario 2: All habits are inactive
                val inactive1 = Habit(name = "Inactive 1", isActive = false, createdAt = Date(), updatedAt = Date())
                val inactive2 = Habit(name = "Inactive 2", isActive = false, createdAt = Date(), updatedAt = Date())
                repository.insertHabit(inactive1)
                repository.insertHabit(inactive2)

                val listAfterInserts = awaitItem()
                assertTrue("Should be empty if all habits inactive", listAfterInserts.isEmpty())

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get active habits should emit updates when inactive habit becomes active`() =
        testScope.runTest {
            val initiallyInactiveHabit =
                Habit(name = "Become Active", isActive = false, createdAt = Date(), updatedAt = Date())
            val habitId = repository.insertHabit(initiallyInactiveHabit)

            repository.getActiveHabits().test {
                assertTrue("Initially no active habits", awaitItem().isEmpty())

                val habitToActivate = repository.getHabitById(habitId)!!.copy(isActive = true, updatedAt = Date())
                repository.updateHabit(habitToActivate)

                val listAfterActivation = awaitItem()
                assertEquals("Should be one active habit", 1, listAfterActivation.size)
                assertEquals(habitId, listAfterActivation.first().habitId)
                assertTrue(listAfterActivation.first().isActive)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get active habits should emit updates when active habit becomes inactive`() =
        testScope.runTest {
            val initiallyActiveHabit =
                Habit(name = "Become Inactive", isActive = true, createdAt = Date(), updatedAt = Date())
            val habitId = repository.insertHabit(initiallyActiveHabit)

            repository.getActiveHabits().test {
                assertEquals("Initially one active habit", 1, awaitItem().size)

                repository.setHabitInactive(habitId) // Using the dedicated method

                assertTrue("Should be no active habits after inactivation", awaitItem().isEmpty())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `set habit inactive should mark active habit as inactive`() =
        testScope.runTest {
            val habit = Habit(name = "Plan day", isActive = true, createdAt = Date(), updatedAt = Date())
            val habitId = repository.insertHabit(habit)

            var retrievedHabit = repository.getHabitById(habitId)
            assertTrue("Habit should initially be active", retrievedHabit?.isActive == true)

            repository.setHabitInactive(habitId)
            testDispatcher.scheduler.advanceUntilIdle()

            retrievedHabit = repository.getHabitById(habitId)
            assertNotNull(retrievedHabit)
            assertFalse("Habit should be inactive after calling setHabitInactive", retrievedHabit?.isActive != false)
        }

    @Test
    fun `set habit inactive should keep already inactive habit as inactive`() =
        testScope.runTest {
            val habit = Habit(name = "Stretch", isActive = false, createdAt = Date(), updatedAt = Date())
            val habitId = repository.insertHabit(habit)

            repository.setHabitInactive(habitId) // Call on already inactive
            testDispatcher.scheduler.advanceUntilIdle()

            val retrievedHabit = repository.getHabitById(habitId)
            assertNotNull(retrievedHabit)
            assertFalse("Habit should remain inactive", retrievedHabit?.isActive != false)
        }

    @Test
    fun `set habit inactive should not affect other habits when non-existent id is used`() =
        testScope.runTest {
            val habit1 = Habit(name = "Review Goals", createdAt = Date(), updatedAt = Date())
            val id1 = repository.insertHabit(habit1)
            val initialCount = repository.getAllHabits().first().size

            repository.setHabitInactive(999L) // Non-existent ID
            testDispatcher.scheduler.advanceUntilIdle()

            val finalCount = repository.getAllHabits().first().size
            assertEquals("Number of habits should not change", initialCount, finalCount)
            val retrievedHabit1 = repository.getHabitById(id1)
            assertNotNull(retrievedHabit1)
            assertTrue("Existing habit should remain active (if it was)", retrievedHabit1?.isActive == true)
        }

    @Test
    fun `set habit inactive should not affect other habits when invalid id is used`() =
        testScope.runTest {
            val habit1 = Habit(name = "Valid Habit", isActive = true, createdAt = Date(), updatedAt = Date())
            val id1 = repository.insertHabit(habit1)

            repository.setHabitInactive(0L)
            testDispatcher.scheduler.advanceUntilIdle()
            var retrieved = repository.getHabitById(id1)
            assertTrue("Habit should still be active after trying to set ID 0 inactive", retrieved?.isActive == true)

            repository.setHabitInactive(-5L)
            testDispatcher.scheduler.advanceUntilIdle()
            retrieved = repository.getHabitById(id1)
            assertTrue(
                "Habit should still be active after trying to set negative ID inactive",
                retrieved?.isActive == true,
            )
        }
}
