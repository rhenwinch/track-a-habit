package io.track.habit.viewmodel

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.track.habit.R
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.utils.StringResource
import io.track.habit.ui.screens.create.CreateViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class CreateViewModelTest {
    private lateinit var habitRepository: HabitRepository
    private lateinit var viewModel: CreateViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        habitRepository = mockk()

        viewModel =
            CreateViewModel(
                habitRepository = habitRepository,
                ioDispatcher = testDispatcher,
            )
    }

    @Test
    fun `updateHabitName should update habitName in state`() =
        runTest(testDispatcher) {
            val testName = "Test Habit"

            viewModel.updateHabitName(testName)

            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state.habitName).isEqualTo(testName)
            }
        }

    @Test
    fun `updateSelectedDate should update selectedDate in state`() =
        runTest(testDispatcher) {
            val testDate = Date(1622505600000) // June 1, 2021

            viewModel.updateSelectedDate(testDate)

            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state.selectedDate).isEqualTo(testDate)
            }
        }

    @Test
    fun `createHabit should validate form and return error when name is blank`() =
        runTest(testDispatcher) {
            viewModel.updateHabitName("")

            viewModel.createHabit()
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state.isNameError).isTrue()
                expectThat(state.isCreationSuccessful).isFalse()
            }
        }

    @Test
    fun `createHabit should call repository when validation passes`() =
        runTest(testDispatcher) {
            val testName = "Test Habit"
            val testDate = Date(1622505600000) // June 1, 2021

            coEvery { habitRepository.insertHabit(any()) } returns 1L

            viewModel.updateHabitName(testName)
            viewModel.updateSelectedDate(testDate)

            viewModel.createHabit()
            advanceUntilIdle()

            coVerify {
                habitRepository.insertHabit(
                    match { habit ->
                        habit.name == testName && habit.lastResetAt == testDate
                    },
                )
            }

            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state.isCreationSuccessful).isTrue()
                expectThat(state.isNameError).isFalse()
            }
        }

    @Test
    fun `createHabit should handle exceptions from repository`() =
        runTest(testDispatcher) {
            val testName = "Test Habit"
            val errorMessage = "Database error"

            coEvery { habitRepository.insertHabit(any()) } throws Exception(errorMessage)

            viewModel.updateHabitName(testName)

            viewModel.createHabit()
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state.isCreationSuccessful).isFalse()

                when (val message = state.errorMessage) {
                    is StringResource.Literal -> expectThat(message.value).isEqualTo(errorMessage)
                    is StringResource.Resource -> expectThat(message.id).isEqualTo(R.string.error_create_habit)
                }
            }
        }

    @Test
    fun `createHabit should handle exceptions with null message`() =
        runTest(testDispatcher) {
            val testName = "Test Habit"

            coEvery { habitRepository.insertHabit(any()) } throws Exception()

            viewModel.updateHabitName(testName)

            viewModel.createHabit()
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state.isCreationSuccessful).isFalse()

                when (val message = state.errorMessage) {
                    is StringResource.Literal -> expectThat(message.value).isEqualTo("")
                    is StringResource.Resource -> expectThat(message.id).isEqualTo(R.string.error_create_habit)
                }
            }
        }
}
