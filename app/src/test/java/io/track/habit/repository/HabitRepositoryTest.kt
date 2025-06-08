package io.track.habit.repository

import android.database.sqlite.SQLiteConstraintException
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.utils.SortOrder
import io.track.habit.repository.fake.FakeHabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class HabitRepositoryTest {
    private lateinit var repository: HabitRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        repository = FakeHabitRepository()
    }

    @Test
    fun `insert habit should add habit to repository and return positive id`() =
        runTest(testDispatcher) {
            val habitName = "Read a book"
            val newHabit = Habit(name = habitName)
            val habitId = repository.insertHabit(newHabit)

            expectThat(habitId).isGreaterThan(0L)
            val retrievedHabit = repository.getHabitById(habitId)
            expectThat(retrievedHabit) {
                isNotNull()
                get { this!!.name }.isEqualTo(habitName)
            }
        }

    @Test
    fun `insert habit should fail when duplicate name is provided`() =
        runTest(testDispatcher) {
            val habitName = "Unique Name"
            val habit1 = Habit(name = habitName, createdAt = Date())
            val id1 = repository.insertHabit(habit1)
            expectThat(id1).isGreaterThan(0L)

            val habit2 = Habit(name = habitName, createdAt = Date())

            expectThrows<SQLiteConstraintException> {
                repository.insertHabit(habit2)
            }

            val habits = repository.getAllHabits().first()
            expectThat(habits.count { it.name == habitName }).isEqualTo(1)
        }

    @Test
    fun `delete habit should remove habit from repository`() =
        runTest(testDispatcher) {
            val habit = Habit(name = "Exercise")
            val habitId = repository.insertHabit(habit)

            val habitToDelete = repository.getHabitById(habitId)!!
            repository.deleteHabit(habitToDelete)

            val retrievedHabit = repository.getHabitById(habitId)
            expectThat(retrievedHabit).isNull()
        }

    @Test
    fun `delete habit should not affect other habits when non-existent habit is deleted`() =
        runTest(testDispatcher) {
            val existingHabit = Habit(name = "Meditate")
            val existingHabitId = repository.insertHabit(existingHabit)

            val nonExistentHabit = Habit(habitId = 999L, name = "Non Existent")
            repository.deleteHabit(nonExistentHabit)

            val retrievedExistingHabit = repository.getHabitById(existingHabitId)
            expectThat(retrievedExistingHabit).isNotNull()
            val allHabits = repository.getAllHabits().first()
            expectThat(allHabits).hasSize(1)
        }

    @Test
    fun `update habit should not create new habit when non-existent habit is updated`() =
        runTest(testDispatcher) {
            val existingHabit = Habit(name = "Journaling")
            repository.insertHabit(existingHabit)
            val initialCount = repository.getAllHabits().first().size

            val nonExistentHabit = Habit(habitId = 999L, name = "Ghost Habit")
            repository.updateHabit(nonExistentHabit)

            expectThat(repository.getAllHabits().first()).hasSize(initialCount)
            expectThat(repository.getHabitById(999L)).isNull()
        }

    @Test
    fun `get all habits should return empty list when no habits exist`() =
        runTest(testDispatcher) {
            repository.getAllHabits().test {
                expectThat(expectMostRecentItem()).isEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should return all inserted habits`() =
        runTest(testDispatcher) {
            val habit1 = Habit(name = "Yoga")
            val habit2 = Habit(name = "Coding practice")
            repository.insertHabit(habit1)
            repository.insertHabit(habit2)

            repository.getAllHabits().test {
                val list = expectMostRecentItem()
                expectThat(list) {
                    hasSize(2)
                }
                expectThat(list.map { it.name }).contains(habit1.name, habit2.name)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should emit updates when habits are added`() =
        runTest(testDispatcher) {
            repository.getAllHabits().test {
                expectThat(awaitItem()).isEmpty()

                val habit1 = Habit(name = "New Habit 1")
                val id1 = repository.insertHabit(habit1)

                val listAfterInsert1 = expectMostRecentItem()
                expectThat(listAfterInsert1) {
                    hasSize(1)
                }
                expectThat(listAfterInsert1.first().habitId).isEqualTo(id1)

                val habit2 = Habit(name = "New Habit 2")
                val id2 = repository.insertHabit(habit2)

                val listAfterInsert2 = awaitItem()
                expectThat(listAfterInsert2) {
                    hasSize(2)
                }
                expectThat(listAfterInsert2.map { it.habitId }).contains(id2)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should emit updates when habits are deleted`() =
        runTest(testDispatcher) {
            val habit1 = Habit(name = "To Delete")
            val habit2 = Habit(name = "To Keep")
            val id1 = repository.insertHabit(habit1)
            val id2 = repository.insertHabit(habit2)

            repository.getAllHabits().test {
                expectThat(expectMostRecentItem()).hasSize(2)

                val habitToDelete = repository.getHabitById(id1)!!
                repository.deleteHabit(habitToDelete)

                val listAfterDelete = awaitItem()
                expectThat(listAfterDelete) {
                    hasSize(1)
                }
                expectThat(listAfterDelete.first().habitId).isEqualTo(id2)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should emit updates when habits are updated`() =
        runTest(testDispatcher) {
            val initialName = "Original Name"
            val updatedName = "Updated Name"
            val habit = Habit(name = initialName)
            val habitId = repository.insertHabit(habit)

            repository.getAllHabits().test {
                var currentList = expectMostRecentItem()
                expectThat(currentList.first { it.habitId == habitId }.name).isEqualTo(initialName)

                val habitToUpdate = repository.getHabitById(habitId)!!.copy(name = updatedName)
                repository.updateHabit(habitToUpdate)

                currentList = awaitItem()
                expectThat(currentList.first { it.habitId == habitId }.name).isEqualTo(updatedName)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get all habits should be sorted by name`() =
        runTest(testDispatcher) {
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
                    expectThat(list).containsExactly(habit2, habit1)
                }

                ascendingTurbine.cancelAndConsumeRemainingEvents()

                with(descendingTurbine) {
                    val list = awaitItem()
                    expectThat(list).containsExactly(habit1, habit2)
                    cancelAndConsumeRemainingEvents()
                }
            }
        }

    @Test
    fun `get all habits should be sorted by creation date`() =
        runTest(testDispatcher) {
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
                    expectThat(list).containsExactly(habit2, habit1)
                    cancelAndConsumeRemainingEvents()
                }

                with(descendingTurbine) {
                    val list = expectMostRecentItem()
                    expectThat(list).containsExactly(habit1, habit2)
                    cancelAndConsumeRemainingEvents()
                }
            }
        }

    @Test
    fun `get all habits should be sorted by streak`() =
        runTest(testDispatcher) {
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
                    expectThat(list).containsExactly(habit1, habit2)
                    cancelAndConsumeRemainingEvents()
                }

                with(descendingTurbine) {
                    val list = expectMostRecentItem()
                    expectThat(list).containsExactly(habit2, habit1)
                    cancelAndConsumeRemainingEvents()
                }
            }
        }

    @Test
    fun `get habit by id should return habit when valid id is provided`() =
        runTest(testDispatcher) {
            val habitName = "Clean room"
            val habit = Habit(name = habitName)
            val habitId = repository.insertHabit(habit)

            val retrievedHabit = repository.getHabitById(habitId)
            expectThat(retrievedHabit) {
                isNotNull()
                get { this!!.name }.isEqualTo(habitName)
                get { this!!.habitId }.isEqualTo(habitId)
            }
        }

    @Test
    fun `get habit by id should return null when non-existent id is provided`() =
        runTest(testDispatcher) {
            val retrievedHabit = repository.getHabitById(12345L)
            expectThat(retrievedHabit).isNull()
        }

    @Test
    fun `get habit by id should return null when invalid id is provided`() =
        runTest(testDispatcher) {
            var retrievedHabit = repository.getHabitById(0L)
            expectThat(retrievedHabit).isNull()

            retrievedHabit = repository.getHabitById(-10L)
            expectThat(retrievedHabit).isNull()
        }

    @Test
    fun `get habit by id flow should emit habit when valid id is provided`() =
        runTest(testDispatcher) {
            val habitName = "Walk dog"
            val habit = Habit(name = habitName)
            val habitId = repository.insertHabit(habit)

            repository.getHabitByIdFlow(habitId).test {
                val emittedHabit = expectMostRecentItem()
                expectThat(emittedHabit) {
                    isNotNull()
                    get { this!!.name }.isEqualTo(habitName)
                    get { this!!.habitId }.isEqualTo(habitId)
                }
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get habit by id flow should return null when non-existent id is provided`() =
        runTest(testDispatcher) {
            repository.getHabitByIdFlow(54321L).test {
                expectThat(awaitItem()).isNull()
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get habit by id flow should emit updates when habit is updated`() =
        runTest(testDispatcher) {
            val initialName = "Initial Flow Name"
            val updatedName = "Updated Flow Name"
            val habit = Habit(name = initialName)
            val habitId = repository.insertHabit(habit)

            repository.getHabitByIdFlow(habitId).test {
                expectThat(expectMostRecentItem()) {
                    isNotNull()
                    get { this!!.name }.isEqualTo(initialName)
                }

                val habitToUpdate = repository.getHabitById(habitId)!!.copy(name = updatedName)
                repository.updateHabit(habitToUpdate)

                expectThat(awaitItem()) {
                    isNotNull()
                    get { this!!.name }.isEqualTo(updatedName)
                }
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `get habit by id flow should emit null after habit is deleted`() =
        runTest(testDispatcher) {
            val habit = Habit(name = "To be deleted via flow")
            val habitId = repository.insertHabit(habit)

            repository.getHabitByIdFlow(habitId).test {
                expectThat(expectMostRecentItem()).isNotNull()

                val habitToDelete = repository.getHabitById(habitId)!!
                repository.deleteHabit(habitToDelete)

                expectThat(awaitItem()).isNull()
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `getLongestStreakInDays should return correct maximum streak value`() =
        runTest(testDispatcher) {
            val now = Date()
            val threeDaysAgo = Date(now.time - (3 * 24 * 60 * 60 * 1000))
            val fiveDaysAgo = Date(now.time - (5 * 24 * 60 * 60 * 1000))

            repository.insertHabit(Habit(name = "Recent habit", lastResetAt = now))
            repository.insertHabit(Habit(name = "Medium streak habit", lastResetAt = threeDaysAgo))
            repository.insertHabit(Habit(name = "Longest streak habit", lastResetAt = fiveDaysAgo))

            repository.getLongestStreakInDays().test {
                val longestStreak = expectMostRecentItem()
                expectThat(longestStreak).isEqualTo(5)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `getLongestStreakInDays should return zero when no habits exist`() =
        runTest(testDispatcher) {
            repository.getLongestStreakInDays().test {
                expectThat(expectMostRecentItem()).isEqualTo(0)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `getLongestStreakInDays should update when habit with longer streak is added`() =
        runTest(testDispatcher) {
            val now = Date()
            val sevenDaysAgo = Date(now.time - (7 * 24 * 60 * 60 * 1000))

            repository.getLongestStreakInDays().test {
                expectThat(expectMostRecentItem()).isEqualTo(0)

                repository.insertHabit(Habit(name = "Medium streak", lastResetAt = sevenDaysAgo))
                advanceUntilIdle()

                expectThat(awaitItem()).isEqualTo(7)

                val tenDaysAgo = Date(now.time - (10 * 24 * 60 * 60 * 1000))
                repository.insertHabit(Habit(name = "Longest streak", lastResetAt = tenDaysAgo))
                advanceUntilIdle()

                expectThat(awaitItem()).isEqualTo(10)
                cancelAndConsumeRemainingEvents()
            }
        }
}
