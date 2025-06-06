package io.track.habit.viewmodel

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import io.mockk.coEvery
import io.mockk.mockk
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.datastore.FakeSettingsDataStore
import io.track.habit.domain.repository.AssetReader
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.usecase.GetHabitsWithStreaksUseCase
import io.track.habit.domain.usecase.GetRandomQuoteUseCase
import io.track.habit.domain.usecase.GetStreaksByDaysUseCase
import io.track.habit.domain.usecase.QUOTES_FILE_NAME
import io.track.habit.domain.utils.SortOrder
import io.track.habit.repository.fake.FakeHabitRepository
import io.track.habit.repository.fake.FakeStreakRepository
import io.track.habit.ui.screens.habits.HabitsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.Date

/**
 * Unit tests for the [HabitsViewModel].
 * This class tests the various functionalities of the [HabitsViewModel], including:
 * - Initialization and UI state updates.
 * - Habit retrieval, addition, updating, and deletion.
 * - Selection and deselection of habits.
 * - Sorting and filtering of habits.
 * - Toggling of UI dialogs and flags.
 * - Censorship of habit names.
 *
 * It utilizes fake repositories and a test dispatcher to ensure isolated and controlled testing environments.
 *
 * For flow testing, we use turbine. We also add delay of 250ms for each collection since its shit
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModelTest {
    private lateinit var assetReader: AssetReader
    private lateinit var habitRepository: HabitRepository
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
        runTest {
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
            val getRandomQuoteUseCase = GetRandomQuoteUseCase(assetReader, testDispatcher)
            val streakRepository = FakeStreakRepository()

            val getStreaksByDaysUseCase = GetStreaksByDaysUseCase(streakRepository)
            val getHabitsWithStreaksUseCase =
                GetHabitsWithStreaksUseCase(
                    habitRepository = habitRepository,
                    getStreakByDaysUseCase = getStreaksByDaysUseCase,
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
                    ioDispatcher = testDispatcher,
                )

            advanceUntilIdle()
        }

    @Test
    fun `getUiState reflects quote update after init`() =
        assert(
            viewModel.uiState.value.quote.message == "Test Quote",
        )

    @Test
    fun `getHabits returns initial empty list`() {
        assert(viewModel.habits.value.isEmpty())
    }

    @Test
    fun `getHabits emits habits from use case`() =
        runTest {
            for (i in 1..5) {
                habitRepository.insertHabit(
                    habit =
                        Habit(
                            name = "Habit $i",
                            lastResetAt = Date().apply { time -= ((i + 3) * 24 * 60 * 60 * 1000) },
                        ),
                )
            }

            viewModel.habits.test {
                skipItems(1)
                val habits = awaitItem()
                assert(habits.isNotEmpty())

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `deleteHabit calls repository delete`() =
        runTest {
            val habit = Habit(habitId = 1, name = "Test habit")
            habitRepository.insertHabit(habit)

            val insertedHabit = habitRepository.getHabitById(habit.habitId)
            assert(insertedHabit != null)

            viewModel.deleteHabit(habit)

            val deletedHabit = habitRepository.getHabitById(habit.habitId)
            assert(deletedHabit == null)
        }

    @Test
    fun `delete selected habits and unselects it all`() =
        runTest {
            for (i in 1..5) {
                habitRepository.insertHabit(
                    habit =
                        Habit(
                            name = "Habit $i",
                            lastResetAt =
                                Date().apply {
                                    time -= ((i + 3) * 24 * 60 * 60 * 1000)
                                },
                        ),
                )
            }

            turbineScope {
                val habitsTurbine = viewModel.habits.testIn(backgroundScope)
                val uiStateTurbine = viewModel.uiState.testIn(backgroundScope)

                with(habitsTurbine) {
                    skipItems(1)
                    awaitItem().forEach {
                        viewModel.toggleSelectionOnHabit(it)
                    }
                }

                with(uiStateTurbine) {
                    val selectedHabits = expectMostRecentItem().selectedHabits
                    assert(selectedHabits.size == 5)

                    selectedHabits.forEach {
                        habitRepository.deleteHabit(it.habit)
                    }

                    viewModel.unselectAll()

                    val updatedSelectedHabits = awaitItem().selectedHabits
                    assert(updatedSelectedHabits.isEmpty())

                    cancelAndConsumeRemainingEvents()
                }

                advanceUntilIdle()
                assert(habitsTurbine.expectMostRecentItem().isEmpty())
                habitsTurbine.cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `toggleSelectionOnHabit adds unselected habit`() =
        runTest {
            for (i in 1..5) {
                habitRepository.insertHabit(
                    habit =
                        Habit(
                            name = "Habit $i",
                            lastResetAt = Date().apply { time -= ((i + 3) * 24 * 60 * 60 * 1000) },
                        ),
                )
            }

            turbineScope {
                val habitsTurbine = viewModel.habits.testIn(backgroundScope)
                val uiStateTurbine = viewModel.uiState.testIn(backgroundScope)

                with(habitsTurbine) {
                    skipItems(1)
                    val habits = awaitItem()
                    viewModel.toggleSelectionOnHabit(habits[0])
                    cancelAndConsumeRemainingEvents()
                }

                with(uiStateTurbine) {
                    skipItems(1)
                    val selectedHabits = expectMostRecentItem().selectedHabits
                    assert(selectedHabits.first().habit.name == "Habit 1")
                    cancelAndConsumeRemainingEvents()
                }
            }
        }

    @Test
    fun `toggleSelectionOnHabit removes selected habit`() =
        runTest {
            for (i in 1..5) {
                habitRepository.insertHabit(
                    habit =
                        Habit(
                            name = "Habit $i",
                            lastResetAt = Date().apply { time -= ((i + 3) * 24 * 60 * 60 * 1000) },
                        ),
                )
            }

            turbineScope {
                val habitsTurbine = viewModel.habits.testIn(backgroundScope)
                val uiStateTurbine = viewModel.uiState.testIn(backgroundScope)

                with(habitsTurbine) {
                    skipItems(1)
                    val habits = awaitItem()
                    viewModel.toggleSelectionOnHabit(habits[0])
                    cancelAndConsumeRemainingEvents()
                }

                with(uiStateTurbine) {
                    skipItems(1)
                    val selectedHabits = awaitItem().selectedHabits
                    assert(selectedHabits.first().habit.name == "Habit 1")

                    viewModel.toggleSelectionOnHabit(selectedHabits.first())

                    val updatedSelectedHabits = awaitItem().selectedHabits
                    assert(updatedSelectedHabits.isEmpty())
                    cancelAndConsumeRemainingEvents()
                }
            }
        }

    @Test
    fun `unselectAll clears selected habits`() =
        runTest {
            for (i in 1..5) {
                habitRepository.insertHabit(
                    habit =
                        Habit(
                            name = "Habit $i",
                            lastResetAt = Date().apply { time -= ((i + 3) * 24 * 60 * 60 * 1000) },
                        ),
                )
            }

            turbineScope {
                val habitsTurbine = viewModel.habits.testIn(backgroundScope)
                val uiStateTurbine = viewModel.uiState.testIn(backgroundScope)

                with(habitsTurbine) {
                    skipItems(1)
                    val habits = awaitItem()
                    habits.forEach {
                        viewModel.toggleSelectionOnHabit(it)
                    }

                    cancelAndConsumeRemainingEvents()
                }

                with(uiStateTurbine) {
                    val selectedHabits = expectMostRecentItem().selectedHabits
                    assert(selectedHabits.size == 5)

                    viewModel.unselectAll()

                    val updatedSelectedHabits = awaitItem().selectedHabits
                    assert(updatedSelectedHabits.isEmpty())

                    cancelAndConsumeRemainingEvents()
                }
            }
        }

    @Test
    fun `toggleSortOrder updates sort order in UiState`() =
        runTest {
            viewModel.toggleSortOrder(SortOrder.Name())
            viewModel.uiState.test {
                val updatedSortOrder = expectMostRecentItem().sortOrder
                assert(updatedSortOrder is SortOrder.Name)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `toggleDeleteConfirmation updates flag in UiState`() =
        runTest {
            viewModel.toggleDeleteConfirmation(true)
            viewModel.uiState.test {
                val updatedFlag = expectMostRecentItem().isShowingDeleteConfirmation
                assert(updatedFlag)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `toggleAddDialog updates flag in UiState`() =
        runTest {
            viewModel.toggleAddDialog(true)
            viewModel.uiState.test {
                val updatedFlag = expectMostRecentItem().isShowingAddDialog
                assert(updatedFlag)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `toggleEditDialog updates flag in UiState`() =
        runTest {
            viewModel.toggleEditDialog(true)
            viewModel.uiState.test {
                val updatedFlag = expectMostRecentItem().isShowingEditDialog
                assert(updatedFlag)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `toggleCensorshipOnNames updates flag in UiState`() =
        runTest {
            viewModel.toggleCensorshipOnNames(true)
            viewModel.uiState.test {
                val updatedFlag = expectMostRecentItem().isCensoringHabitNames
                assert(updatedFlag)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `getHabits have censors applied when requested with censoring enabled`() =
        runTest {
            viewModel.toggleCensorshipOnNames(true)

            for (i in 1..5) {
                habitRepository.insertHabit(
                    habit =
                        Habit(
                            name = "Habit $i",
                            lastResetAt = Date().apply { time -= ((i + 3) * 24 * 60 * 60 * 1000) },
                        ),
                )
            }

            viewModel.habits.test {
                val habits = expectMostRecentItem()

                assert(habits.all { it.habit.name.contains("*") })
            }
        }

    @Test
    fun `getHabits have censors applied when requested with censoring disabled`() =
        runTest {
            viewModel.toggleCensorshipOnNames(false)

            for (i in 1..5) {
                habitRepository.insertHabit(
                    habit =
                        Habit(
                            name = "Habit $i",
                            lastResetAt = Date().apply { time -= ((i + 3) * 24 * 60 * 60 * 1000) },
                        ),
                )
            }

            viewModel.habits.test {
                val habits = expectMostRecentItem()

                assert(habits.all { !it.habit.name.contains("*") })
            }
        }
}
