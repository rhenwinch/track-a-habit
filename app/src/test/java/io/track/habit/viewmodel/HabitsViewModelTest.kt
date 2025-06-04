package io.track.habit.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.datastore.FakeSettingsDataStore
import io.track.habit.domain.repository.AssetReader
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.repository.StreakRepository
import io.track.habit.domain.usecase.GetHabitsWithStreaksUseCase
import io.track.habit.domain.usecase.GetRandomQuoteUseCase
import io.track.habit.domain.usecase.GetStreaksByDaysUseCase
import io.track.habit.domain.usecase.QUOTES_FILE_NAME
import io.track.habit.domain.utils.SortOrder
import io.track.habit.repository.fake.FakeHabitRepository
import io.track.habit.repository.fake.FakeStreakRepository
import io.track.habit.ui.screens.habits.HabitsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.Date

class HabitsViewModelTest {
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var assetReader: AssetReader
    private lateinit var getHabitsWithStreaksUseCase: GetHabitsWithStreaksUseCase
    private lateinit var getStreaksByDaysUseCase: GetStreaksByDaysUseCase
    private lateinit var getRandomQuoteUseCase: GetRandomQuoteUseCase
    private lateinit var habitRepository: HabitRepository
    private lateinit var streakRepository: StreakRepository
    private lateinit var viewModel: HabitsViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @get:Rule
    val temporaryFolder: TemporaryFolder =
        TemporaryFolder
            .builder()
            .assureDeletion()
            .build()

    @Before
    fun setUp() {
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
        streakRepository = FakeStreakRepository()
        getRandomQuoteUseCase = GetRandomQuoteUseCase(assetReader)

        getStreaksByDaysUseCase = GetStreaksByDaysUseCase(streakRepository)
        getHabitsWithStreaksUseCase =
            GetHabitsWithStreaksUseCase(
                habitRepository = habitRepository,
                getStreakByDaysUseCase = getStreaksByDaysUseCase,
            )

        testDataStore =
            PreferenceDataStoreFactory.create(
                scope = testScope,
                produceFile = { temporaryFolder.newFile("test_tah_settings.preferences_pb") },
            )

        viewModel =
            HabitsViewModel(
                getHabitsWithStreaksUseCase = getHabitsWithStreaksUseCase,
                getRandomQuoteUseCase = getRandomQuoteUseCase,
                habitRepository = habitRepository,
                settingsDataStore = FakeSettingsDataStore(testDataStore),
            )
    }

    @Test
    fun `getUiState reflects quote update after init`() {
        assert(
            viewModel.uiState.value.quote.message == "Test Quote",
        )
    }

    @Test
    fun `getHabits returns initial empty list`() {
        assert(viewModel.habits.value.isEmpty())
    }

    @Test
    fun `getHabits emits habits from use case`() =
        testScope.runTest {
            viewModel.habits.test {
                awaitItem() // Initialize collection

                for (i in 1..5) {
                    habitRepository.insertHabit(
                        habit =
                            Habit(
                                name = "Habit $i",
                                lastResetAt = Date().apply { time -= ((i + 3) * 24 * 60 * 60 * 1000) },
                            ),
                    )
                }

                val habits = awaitItem()
                assert(habits.isNotEmpty())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `deleteHabit calls repository delete`() =
        testScope.runTest {
            val habit = Habit(habitId = 1, name = "Test habit")
            habitRepository.insertHabit(habit)

            val insertedHabit = habitRepository.getHabitById(habit.habitId)
            assert(insertedHabit != null)

            viewModel.deleteHabit(habit)

            val deletedHabit = habitRepository.getHabitById(habit.habitId)
            assert(deletedHabit == null)
        }

    @Test
    fun `deleteSelectedHabits deletes selected habits and unselects`() =
        testScope.runTest {
            viewModel.habits.test {
                awaitItem() // Initialize collection

                for (i in 1..5) {
                    habitRepository.insertHabit(
                        habit =
                            Habit(
                                name = "Habit $i",
                                lastResetAt = Date().apply { time -= ((i + 3) * 24 * 60 * 60 * 1000) },
                            ),
                    )
                }

                val habits = awaitItem()
                habits.forEach {
                    viewModel.toggleSelectionOnHabit(it)
                }

                cancelAndIgnoreRemainingEvents()
            }

            viewModel.uiState.test {
                val selectedHabits = awaitItem().selectedHabits
                assert(selectedHabits.size == 5)
                viewModel.deleteSelectedHabits()

                val updatedSelectedHabits = awaitItem().selectedHabits
                assert(updatedSelectedHabits.isEmpty())
            }

            viewModel.habits.test {
                val habits = awaitItem()

                assert(habits.isEmpty())
            }
        }

    @Test
    fun `addHabit calls repository insert`() =
        testScope.runTest {
            viewModel.habits.test {
                awaitItem() // Initialize collection

                val habit = Habit(habitId = 1, name = "Test habit")
                viewModel.addHabit(habit)

                val habits = awaitItem()
                assert(habits.any { it.habit == habit })

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `updateHabit calls repository update`() =
        testScope.runTest {
            viewModel.habits.test {
                awaitItem() // Initialize collection

                val habit = Habit(name = "Test habit")
                val habitId = habitRepository.insertHabit(habit)

                val habits = awaitItem()
                assert(habits.any { it.habit.name == "Test habit" })

                val updatedHabit = habit.copy(habitId = habitId, name = "Updated habit")
                viewModel.updateHabit(updatedHabit)

                val updatedHabits = awaitItem()
                assert(updatedHabits.any { it.habit.name == "Updated habit" })

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggleSelectionOnHabit adds unselected habit`() =
        testScope.runTest {
            viewModel.habits.test {
                awaitItem() // Initialize collection

                for (i in 1..5) {
                    habitRepository.insertHabit(
                        habit =
                            Habit(
                                name = "Habit $i",
                                lastResetAt = Date().apply { time -= ((i + 3) * 24 * 60 * 60 * 1000) },
                            ),
                    )
                }

                val habits = awaitItem()
                viewModel.toggleSelectionOnHabit(habits[0])
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.uiState.test {
                val selectedHabits = awaitItem().selectedHabits
                assert(selectedHabits.first().habit.name == "Habit 1")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggleSelectionOnHabit removes selected habit`() =
        testScope.runTest {
            viewModel.habits.test {
                awaitItem() // Initialize collection

                for (i in 1..5) {
                    habitRepository.insertHabit(
                        habit =
                            Habit(
                                name = "Habit $i",
                                lastResetAt = Date().apply { time -= ((i + 3) * 24 * 60 * 60 * 1000) },
                            ),
                    )
                }

                val habits = awaitItem()
                viewModel.toggleSelectionOnHabit(habits[0])
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.uiState.test {
                val selectedHabits = awaitItem().selectedHabits
                assert(selectedHabits.first().habit.name == "Habit 1")

                viewModel.toggleSelectionOnHabit(selectedHabits.first())
                val updatedSelectedHabits = awaitItem().selectedHabits
                assert(updatedSelectedHabits.isEmpty())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `unselectAll clears selected habits`() =
        testScope.runTest {
            viewModel.habits.test {
                awaitItem() // Initialize collection

                for (i in 1..5) {
                    habitRepository.insertHabit(
                        habit =
                            Habit(
                                name = "Habit $i",
                                lastResetAt = Date().apply { time -= ((i + 3) * 24 * 60 * 60 * 1000) },
                            ),
                    )
                }

                val habits = awaitItem()
                habits.forEach {
                    viewModel.toggleSelectionOnHabit(it)
                }

                cancelAndIgnoreRemainingEvents()
            }

            viewModel.uiState.test {
                val selectedHabits = awaitItem().selectedHabits
                assert(selectedHabits.size == 5)

                viewModel.unselectAll()
                val updatedSelectedHabits = awaitItem().selectedHabits
                assert(updatedSelectedHabits.isEmpty())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggleSortOrder updates sort order in UiState`() =
        testScope.runTest {
            viewModel.uiState.test {
                val initialSortOrder = awaitItem().sortOrder
                assert(initialSortOrder is SortOrder.Streak)

                viewModel.toggleSortOrder(SortOrder.Name())
                val updatedSortOrder = awaitItem().sortOrder
                assert(updatedSortOrder is SortOrder.Name)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggleDeleteConfirmation updates flag in UiState`() =
        testScope.runTest {
            viewModel.uiState.test {
                val initialFlag = awaitItem().isShowingDeleteConfirmation
                assert(!initialFlag)

                viewModel.toggleDeleteConfirmation(true)
                val updatedFlag = awaitItem().isShowingDeleteConfirmation
                assert(updatedFlag)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggleAddDialog updates flag in UiState`() =
        testScope.runTest {
            viewModel.uiState.test {
                val initialFlag = awaitItem().isShowingAddDialog
                assert(!initialFlag)

                viewModel.toggleAddDialog(true)
                val updatedFlag = awaitItem().isShowingAddDialog
                assert(updatedFlag)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggleEditDialog updates flag in UiState`() =
        testScope.runTest {
            viewModel.uiState.test {
                val initialFlag = awaitItem().isShowingEditDialog
                assert(!initialFlag)

                viewModel.toggleEditDialog(true)
                val updatedFlag = awaitItem().isShowingEditDialog
                assert(updatedFlag)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggleCensorshipOnNames updates flag in UiState`() =
        testScope.runTest {
            viewModel.uiState.test {
                val initialFlag = awaitItem().isCensoringHabitNames
                assert(!initialFlag)

                viewModel.toggleCensorshipOnNames(true)
                val updatedFlag = awaitItem().isCensoringHabitNames
                assert(updatedFlag)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `getHabits have censors applied when requested with censoring enabled`() =
        testScope.runTest {
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

            val habits = viewModel.habits.first()
            assert(habits.all { it.habit.name.contains("*") })
        }

    @Test
    fun `getHabits have censors applied when requested with censoring disabled`() =
        testScope.runTest {
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

            val habits = viewModel.habits.first()
            assert(habits.all { !it.habit.name.contains("*") })
        }
}
