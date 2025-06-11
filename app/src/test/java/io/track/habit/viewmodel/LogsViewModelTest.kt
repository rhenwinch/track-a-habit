package io.track.habit.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.usecase.GetStreakUseCase
import io.track.habit.repository.fake.FakeHabitLogsRepository
import io.track.habit.repository.fake.FakeStreakRepository
import io.track.habit.ui.navigation.SubNavRoute
import io.track.habit.ui.screens.logs.LogsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import java.util.Date

class SavedStateHandleRule(
    private val route: Any,
) : TestWatcher() {
    val savedStateHandleMock: SavedStateHandle = mockk()

    override fun starting(description: Description?) {
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        every { savedStateHandleMock.toRoute<Any>(any(), any()) } returns route
        super.starting(description)
    }

    override fun finished(description: Description?) {
        unmockkStatic("androidx.navigation.SavedStateHandleKt")
        super.finished(description)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LogsViewModelTest {
    private lateinit var viewModel: LogsViewModel
    private lateinit var habitLogsRepository: HabitLogsRepository
    private lateinit var getStreakUseCase: GetStreakUseCase

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    private val testHabitId = 1L
    private val sampleDate = Date()

    @get:Rule
    val savedStateHandleRule = SavedStateHandleRule(SubNavRoute.HabitsViewLogs(habitId = testHabitId))

    private val sampleLog =
        HabitLog(
            logId = 1L,
            habitId = testHabitId,
            streakDuration = 5,
            createdAt = sampleDate,
            updatedAt = sampleDate,
            trigger = "Test trigger",
            notes = "Test notes",
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val streakRepository = FakeStreakRepository()
        getStreakUseCase = GetStreakUseCase(streakRepository)

        habitLogsRepository = FakeHabitLogsRepository()

        runTest(testDispatcher) {
            habitLogsRepository.insertHabitLog(sampleLog)
        }

        viewModel =
            LogsViewModel(
                habitLogsRepository = habitLogsRepository,
                ioDispatcher = testDispatcher,
                getStreakUseCase = getStreakUseCase,
                savedStateHandle = savedStateHandleRule.savedStateHandleMock,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given habit logs when viewModel initialized then logs state contains transformed data`() =
        runTest(testDispatcher) {
            viewModel.logs.test {
                val emission = awaitItem()
                expectThat(emission) {
                    isNotEmpty()
                    hasSize(1)
                }

                val logWithStreak = emission.first()
                expectThat(logWithStreak) {
                    get { log.logId }.isEqualTo(1L)
                    get { log.streakDuration }.isEqualTo(5)
                    get { streak.title }.isEqualTo("Getting Started")
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when updateLog is called then repository is updated with new log data`() =
        runTest(testDispatcher) {
            val mockRepository = mockk<HabitLogsRepository>(relaxed = true)

            val testViewModel =
                LogsViewModel(
                    habitLogsRepository = mockRepository,
                    ioDispatcher = testDispatcher,
                    getStreakUseCase = getStreakUseCase,
                    savedStateHandle = savedStateHandleRule.savedStateHandleMock,
                )

            val updatedLog = sampleLog.copy(notes = "Updated notes")
            testViewModel.updateLog(updatedLog)

            advanceUntilIdle()

            coVerify { mockRepository.updateHabitLog(updatedLog) }
        }
}
