package io.track.habit.viewmodel

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import io.track.habit.R
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.repository.StreakRepository
import io.track.habit.datastore.FakeSettingsDataStore
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.model.Streak
import io.track.habit.domain.repository.AssetReader
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.usecase.GetHabitsWithStreaksUseCase
import io.track.habit.domain.usecase.GetRandomQuoteUseCase
import io.track.habit.domain.usecase.GetStreakUseCase
import io.track.habit.domain.usecase.QUOTES_FILE_NAME
import io.track.habit.domain.utils.SortOrder
import io.track.habit.domain.utils.drawableRes
import io.track.habit.domain.utils.stringLiteral
import io.track.habit.repository.fake.FakeHabitLogsRepository
import io.track.habit.repository.fake.FakeHabitRepository
import io.track.habit.ui.screens.habits.HabitsViewModel
import io.track.habit.ui.screens.habits.composables.ResetDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModelTest {
    private lateinit var assetReader: AssetReader
    private lateinit var habitRepository: HabitRepository
    private lateinit var habitLogsRepository: HabitLogsRepository
    private lateinit var viewModel: HabitsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val temporaryFolder: TemporaryFolder =
        TemporaryFolder
            .builder()
            .assureDeletion()
            .build()

    @Before
    fun setUp() =
        runTest(testDispatcher) {
            Dispatchers.setMain(testDispatcher)
            assetReader = mockk()
            coEvery { assetReader.read(QUOTES_FILE_NAME) } returns
                """
                [
                    {
                        "message": "Test Quote",
                        "author": "Test Author"
                    }
                ]
                """.trimIndent()

            habitRepository = FakeHabitRepository()
            habitLogsRepository = FakeHabitLogsRepository()

            val getRandomQuoteUseCase = GetRandomQuoteUseCase(assetReader, testDispatcher)
            val streakRepository = StreakRepository()

            val getStreakUseCase = GetStreakUseCase(streakRepository)
            val getHabitsWithStreaksUseCase =
                GetHabitsWithStreaksUseCase(
                    habitRepository = habitRepository,
                    getStreakByDaysUseCase = getStreakUseCase,
                )

            val testDataStore =
                PreferenceDataStoreFactory.create(
                    scope = backgroundScope,
                    produceFile = { temporaryFolder.newFile("test_tah_settings.preferences_pb") },
                )

            val testSettingsDataStore = FakeSettingsDataStore(testDataStore)

            viewModel =
                HabitsViewModel(
                    getHabitsWithStreaksUseCase = getHabitsWithStreaksUseCase,
                    getRandomQuoteUseCase = getRandomQuoteUseCase,
                    habitRepository = habitRepository,
                    settingsDataStore = testSettingsDataStore,
                    habitLogsRepository = habitLogsRepository,
                    ioDispatcher = testDispatcher,
                )

            advanceUntilIdle()
        }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `resetProgress should update habit and create log`() =
        runTest(testDispatcher) {
            val habitId = 1L
            val habit = Habit(
                habitId = habitId,
                name = "Test Habit",
                lastResetAt = Date(System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000L)),
            ) // 5 days ago
            val resetDetails = ResetDetails(habitId = habitId, trigger = "Test Trigger", notes = "Test Notes")

            habitRepository.insertHabit(habit)

            viewModel.resetProgress(resetDetails)
            advanceUntilIdle()

            val updatedHabit = habitRepository.getHabitById(habitId)
            expectThat(updatedHabit).isNotNull()

            habitLogsRepository.getHabitLogsByHabitId(habitId).test {
                val logs = awaitItem()

                expectThat(logs.size).isEqualTo(1)
                expectThat(logs[0]) {
                    get { habitId }.isEqualTo(habitId)
                    get { trigger }.isEqualTo("Test Trigger")
                    get { notes }.isEqualTo("Test Notes")
                }
            }
        }

    @Test
    fun `onHabitLongClick should update longPressedHabit in state`() =
        runTest {
            val streak =
                Streak(
                    title = stringLiteral("Test Streak"),
                    minDaysRequired = 0,
                    maxDaysRequired = 10,
                    badgeIcon = drawableRes(R.drawable.grin_with_sweat_emoji),
                    message = stringLiteral("Test message"),
                )

            val habitWithStreak =
                HabitWithStreak(
                    habit = Habit(habitId = 1, name = "Test Habit"),
                    streak = streak,
                )

            viewModel.onHabitLongClick(habitWithStreak)

            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state.longPressedHabit).isSameInstanceAs(habitWithStreak)
            }
        }

    @Test
    fun `onSortOrderSelect should update sortOrder in state`() =
        runTest {
            val newSortOrder = SortOrder.Creation()

            viewModel.onSortOrderSelect(newSortOrder)

            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state.sortOrder).isEqualTo(newSortOrder)
            }
        }

    @Test
    fun `toggleCensorshipOnNames should update isCensoringHabitNames in state`() =
        runTest {
            val censorship = true

            viewModel.toggleCensorshipOnNames(censorship)

            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state.isCensoringHabitNames).isEqualTo(censorship)
            }
        }

    @Test
    fun `toggleShowcaseHabit should update habitToShowcase in state and update settings`() =
        runTest(testDispatcher) {
            val streak =
                Streak(
                    title = stringLiteral("Test Streak"),
                    minDaysRequired = 0,
                    maxDaysRequired = 10,
                    badgeIcon = drawableRes(R.drawable.you_rock_emoji),
                    message = stringLiteral("Test message"),
                )

            val habitWithStreak =
                HabitWithStreak(
                    habit = Habit(habitId = 1, name = "Test Habit"),
                    streak = streak,
                )

            viewModel.toggleShowcaseHabit(habitWithStreak)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state.habitToShowcase).isSameInstanceAs(habitWithStreak)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `habits flow should emit habits from repository`() =
        runTest(testDispatcher) {
            val habit1 = Habit(habitId = 1, name = "Test Habit 1")
            val habit2 = Habit(habitId = 2, name = "Test Habit 2")

            habitRepository.insertHabit(habit1)
            habitRepository.insertHabit(habit2)

            advanceUntilIdle()

            viewModel.habits.test {
                val result = awaitItem()
                expectThat(result.size).isEqualTo(2)
                expectThat(result[0].habit.habitId).isEqualTo(1L)
                expectThat(result[1].habit.habitId).isEqualTo(2L)
            }
        }
}
