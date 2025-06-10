package io.track.habit.viewmodel

import app.cash.turbine.test
import io.track.habit.R
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.domain.model.Streak
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.repository.StreakRepository
import io.track.habit.domain.usecase.GetAllTimeStreakUseCase
import io.track.habit.domain.usecase.GetHabitsWithStreaksUseCase
import io.track.habit.domain.usecase.GetStreakUseCase
import io.track.habit.domain.utils.StringResource
import io.track.habit.repository.fake.FakeHabitLogsRepository
import io.track.habit.repository.fake.FakeHabitRepository
import io.track.habit.repository.fake.FakeStreakRepository
import io.track.habit.ui.screens.streaks.StreakViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class StreakViewModelTest {
    private lateinit var viewModel: StreakViewModel
    private lateinit var habitRepository: HabitRepository
    private lateinit var habitLogsRepository: HabitLogsRepository
    private lateinit var streakRepository: StreakRepository
    private lateinit var getHabitsWithStreaksUseCase: GetHabitsWithStreaksUseCase
    private lateinit var getAllTimeStreakUseCase: GetAllTimeStreakUseCase
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        habitRepository = FakeHabitRepository()
        habitLogsRepository = FakeHabitLogsRepository()
        streakRepository = FakeStreakRepository()

        val getStreakUseCase = GetStreakUseCase(streakRepository)
        getHabitsWithStreaksUseCase = GetHabitsWithStreaksUseCase(habitRepository, getStreakUseCase)
        getAllTimeStreakUseCase =
            GetAllTimeStreakUseCase(getStreakUseCase, getHabitsWithStreaksUseCase, habitLogsRepository)

        viewModel =
            StreakViewModel(
                streakRepository = streakRepository,
                habitRepository = habitRepository,
                habitLogsRepository = habitLogsRepository,
                getHabitsWithStreaksUseCase = getHabitsWithStreaksUseCase,
                getAllTimeStreakUseCase = getAllTimeStreakUseCase,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when no habits exist then all streaks should show zero habits in range`() =
        runTest(testDispatcher) {
            // Let the viewModel collect initial values
            advanceUntilIdle()

            viewModel.streakSummaries.test {
                skipItems(1) // Skip initial empty state
                val summaries = awaitItem()

                // Verify we have all streak milestones from the repository
                expectThat(summaries).hasSize(6)

                // First streak (Getting Started) should be achieved with 0 habits
                expectThat(summaries[0]) {
                    get { isAchieved }.isTrue()
                    // Check that status shows 0 habits in streak
                    get { status }.isA<StringResource.Plural>()
                    get { (status as StringResource.Plural).quantity }.isEqualTo(0)
                }

                // Other streaks should not be achieved yet
                expectThat(summaries.subList(1, summaries.size).all { !it.isAchieved }).isTrue()
            }
        }

    @Test
    fun `when habits exist with various streaks then streak summaries should count them correctly`() =
        runTest(testDispatcher) {
            // Add habits with different streak lengths
            val today = Date()
            val threeDaysAgo = Date(today.time - (3 * 24 * 60 * 60 * 1000))
            val tenDaysAgo = Date(today.time - (10 * 24 * 60 * 60 * 1000))
            val thirtyDaysAgo = Date(today.time - (30 * 24 * 60 * 60 * 1000L))

            // Add habits with various streak values
            habitRepository.insertHabit(Habit(name = "3-day streak habit", lastResetAt = threeDaysAgo))
            habitRepository.insertHabit(Habit(name = "10-day streak habit", lastResetAt = tenDaysAgo))
            habitRepository.insertHabit(Habit(name = "10-day streak habit 2", lastResetAt = tenDaysAgo))
            habitRepository.insertHabit(Habit(name = "30-day streak habit", lastResetAt = thirtyDaysAgo))

            advanceUntilIdle()

            viewModel.streakSummaries.test {
                skipItems(1) // Skip initial empty state
                val summaries = awaitItem()

                // Getting Started (0-6 days): Should have 1 habit
                expectThat(summaries[0]) {
                    get { isAchieved }.isTrue()
                    get { status }.isA<StringResource.Plural>()
                    get { (status as StringResource.Plural).quantity }.isEqualTo(1) // 3-day streak habit
                }

                // One Week Streak (7-13 days: Should have 2 habits
                expectThat(summaries[1]) {
                    get { isAchieved }.isTrue()
                    get { status }.isA<StringResource.Plural>()
                    get { (status as StringResource.Plural).quantity }.isEqualTo(2) // Two 10-day streak habits
                }

                // Two Week Streak (14-20 days): Should have 0 habits
                expectThat(summaries[2]) {
                    get { isAchieved }.isTrue()
                    get { status }.isA<StringResource.Plural>()
                    get { (status as StringResource.Plural).quantity }.isEqualTo(0)
                }

                // One Month Streak (21-34 days): Should have 1 habit
                expectThat(summaries[3]) {
                    get { isAchieved }.isTrue()
                    get { status }.isA<StringResource.Plural>()
                    get { (status as StringResource.Plural).quantity }.isEqualTo(1) // 30-day streak habit
                }

                // Champion (35-59 days): Should have 0 habits and not be achieved
                expectThat(summaries[4]) {
                    get { isAchieved }.isFalse()
                }

                // Legend (60+ days): Should have 0 habits and not be achieved
                expectThat(summaries[5]) {
                    get { isAchieved }.isFalse()
                }
            }
        }

    @Test
    fun `when longest streak exceeds milestone threshold then streak is marked as achieved`() =
        runTest(testDispatcher) {
            // Add a habit with a long streak (70 days)
            val seventyDaysAgo = Date(Date().time - (70 * 24 * 60 * 60 * 1000L))

            habitRepository.insertHabit(Habit(name = "70-day streak habit", lastResetAt = seventyDaysAgo))

            advanceUntilIdle()

            viewModel.streakSummaries.test {
                skipItems(1) // Skip initial empty state
                val summaries = awaitItem()

                // All streaks should be achieved
                summaries.forEach { summary ->
                    expectThat(summary.isAchieved).isTrue()
                }

                // Legend streak (60+ days) should have 1 habit
                expectThat(summaries.last()) {
                    get { status }.isA<StringResource.Plural>()
                    get { (status as StringResource.Plural).quantity }.isEqualTo(1)
                }
            }
        }

    @Test
    fun `when approaching a streak milestone then correct status message is shown`() =
        runTest(testDispatcher) {
            // Create a custom streak repository with a controlled set of milestones
            val customStreakRepository =
                object : StreakRepository {
                    override fun getAllStreaks(): List<Streak> {
                        return listOf(
                            Streak(
                                title = "Test Milestone",
                                minDaysRequired = 10,
                                maxDaysRequired = 20,
                                badgeIcon = "test_badge",
                                message = "Test message",
                            ),
                        )
                    }
                }

            // Create a habit with a streak that's 95% of the way to the milestone
            val almostThereDays = (10 * 0.95).toInt() // 9 days
            val almostThereDate = Date(Date().time - (almostThereDays * 24 * 60 * 60 * 1000))

            habitRepository.insertHabit(Habit(name = "Almost there habit", lastResetAt = almostThereDate))

            // Create a new ViewModel with our custom streak repository
            val getStreakUseCase = GetStreakUseCase(customStreakRepository)
            val customGetHabitsWithStreaksUseCase =
                GetHabitsWithStreaksUseCase(
                    habitRepository,
                    getStreakUseCase,
                )
            val customGetAllTimeStreakUseCase =
                GetAllTimeStreakUseCase(
                    getStreakUseCase,
                    customGetHabitsWithStreaksUseCase,
                    habitLogsRepository,
                )

            val customViewModel =
                StreakViewModel(
                    streakRepository = customStreakRepository,
                    habitRepository = habitRepository,
                    habitLogsRepository = habitLogsRepository,
                    getHabitsWithStreaksUseCase = customGetHabitsWithStreaksUseCase,
                    getAllTimeStreakUseCase = customGetAllTimeStreakUseCase,
                )

            advanceUntilIdle()

            customViewModel.streakSummaries.test {
                skipItems(1) // Skip initial empty state
                val summaries = awaitItem()

                expectThat(summaries).hasSize(1)

                // The streak should not be achieved yet, but showing "almost there" message
                expectThat(summaries[0]) {
                    get { isAchieved }.isFalse()
                    get { status }.isA<StringResource.Resource>()
                    get { (status as StringResource.Resource).id }.isEqualTo(R.string.streak_very_close)
                }
            }
        }

    @Test
    fun `when streak is fully achieved then duration text shows min and max days`() =
        runTest(testDispatcher) {
            // Use a custom streak repository with a controlled set of milestones
            val customStreakRepository =
                object : StreakRepository {
                    override fun getAllStreaks(): List<Streak> {
                        return listOf(
                            Streak(
                                title = "Test Milestone",
                                minDaysRequired = 0,
                                maxDaysRequired = 20,
                                badgeIcon = "test_badge",
                                message = "Test message",
                            ),
                        )
                    }
                }

            // Create a habit with a streak that's beyond the max days
            val beyondMaxDays = 25
            val beyondMaxDate = Date(Date().time - (beyondMaxDays * 24 * 60 * 60 * 1000L))

            habitRepository.insertHabit(Habit(name = "Beyond max days habit", lastResetAt = beyondMaxDate))

            // Create a new ViewModel with our custom streak repository
            val customViewModel =
                StreakViewModel(
                    streakRepository = customStreakRepository,
                    habitRepository = habitRepository,
                    habitLogsRepository = habitLogsRepository,
                    getHabitsWithStreaksUseCase = getHabitsWithStreaksUseCase,
                    getAllTimeStreakUseCase = getAllTimeStreakUseCase,
                )

            advanceUntilIdle()

            customViewModel.streakSummaries.test {
                skipItems(1)
                val summaries = awaitItem()

                expectThat(summaries).hasSize(1)

                // The streak should be achieved and showing full duration text
                expectThat(summaries[0]) {
                    get { isAchieved }.isTrue()
                    // Check that durationText is of type Resource
                    get { durationText }.isA<StringResource.Resource>()
                    get { (durationText as StringResource.Resource).id }.isEqualTo(R.string.streak_days_range)
                }
            }
        }

    @Test
    fun `when no habits exist then highestOngoingStreak should be null`() =
        runTest(testDispatcher) {
            advanceUntilIdle()

            viewModel.highestOngoingStreak.test {
                expectThat(awaitItem()).isNull()
            }
        }

    @Test
    fun `when habits exist then highestOngoingStreak should return habit with longest streak`() =
        runTest(testDispatcher) {
            // Add habits with different streak lengths
            val today = Date()
            val threeDaysAgo = Date(today.time - (3 * 24 * 60 * 60 * 1000))
            val tenDaysAgo = Date(today.time - (10 * 24 * 60 * 60 * 1000))
            val thirtyDaysAgo = Date(today.time - (30 * 24 * 60 * 60 * 1000L))

            // Add habits with various streak values
            habitRepository.insertHabit(Habit(habitId = 1, name = "3-day streak habit", lastResetAt = threeDaysAgo))
            habitRepository.insertHabit(Habit(habitId = 2, name = "10-day streak habit", lastResetAt = tenDaysAgo))
            habitRepository.insertHabit(Habit(habitId = 3, name = "30-day streak habit", lastResetAt = thirtyDaysAgo))

            advanceUntilIdle()

            viewModel.highestOngoingStreak.test {
                skipItems(1) // Skip initial empty state
                val highestStreak = awaitItem()

                expectThat(highestStreak).isNotNull()
                expectThat(highestStreak!!.habit) {
                    get { habitId }.isEqualTo(3) // Should return the 30-day streak habit
                    get { name }.isEqualTo("30-day streak habit")
                    get { streakInDays }.isEqualTo(30)
                }
            }
        }

    @Test
    fun `when multiple habits have same streak then highestOngoingStreak should return first one`() =
        runTest(testDispatcher) {
            // Add two habits with the same streak length (20 days)
            val twentyDaysAgo = Date(Date().time - (20 * 24 * 60 * 60 * 1000L))

            habitRepository.insertHabit(Habit(habitId = 1, name = "First 20-day streak", lastResetAt = twentyDaysAgo))
            habitRepository.insertHabit(Habit(habitId = 2, name = "Second 20-day streak", lastResetAt = twentyDaysAgo))

            viewModel.highestOngoingStreak.test {
                skipItems(1) // Skip initial empty state
                val highestStreak = awaitItem()

                expectThat(highestStreak).isNotNull()
                expectThat(highestStreak!!.habit) {
                    get { habitId }.isEqualTo(1)
                    get { name }.isEqualTo("First 20-day streak")
                    get { streakInDays }.isEqualTo(20)
                }
            }
        }

    @Test
    fun `when no habits exist then highestAllTimeStreak should be null`() =
        runTest(testDispatcher) {
            advanceUntilIdle()

            viewModel.highestAllTimeStreak.test {
                expectThat(awaitItem()).isNull()
            }
        }

    @Test
    fun `when habits exist then highestAllTimeStreak should return habit with longest streak`() =
        runTest(testDispatcher) {
            // Add habits with different streak lengths
            val today = Date()
            val threeDaysAgo = Date(today.time - (3 * 24 * 60 * 60 * 1000))
            val tenDaysAgo = Date(today.time - (10 * 24 * 60 * 60 * 1000))
            val thirtyDaysAgo = Date(today.time - (30 * 24 * 60 * 60 * 1000L))

            // Add habits with various streak values
            habitRepository.insertHabit(Habit(habitId = 1, name = "3-day streak habit", lastResetAt = threeDaysAgo))
            habitRepository.insertHabit(Habit(habitId = 2, name = "10-day streak habit", lastResetAt = tenDaysAgo))
            habitRepository.insertHabit(Habit(habitId = 3, name = "30-day streak habit", lastResetAt = thirtyDaysAgo))

            advanceUntilIdle()

            viewModel.highestAllTimeStreak.test {
                skipItems(1) // Skip initial empty state
                val allTimeStreak = awaitItem()

                expectThat(allTimeStreak).isNotNull()
                expectThat(allTimeStreak!!.streakInDays).isEqualTo(30)
                expectThat(allTimeStreak.endDate).isNull() // Should be an ongoing streak
            }
        }

    @Test
    fun `when habit logs exist with longer streaks than current habits then highestAllTimeStreak should return the longest log`() =
        runTest(testDispatcher) {
            // Setup current habit with modest streak
            val tenDaysAgo = Date(Date().time - (10 * 24 * 60 * 60 * 1000L))
            habitRepository.insertHabit(Habit(habitId = 1, name = "10-day streak habit", lastResetAt = tenDaysAgo))

            // Add a completed habit log with a higher streak
            val today = Date()
            habitLogsRepository.insertHabitLog(
                HabitLog(
                    logId = 1,
                    habitId = 2,
                    streakDuration = 45,
                    createdAt = today,
                ),
            )

            advanceUntilIdle()

            viewModel.highestAllTimeStreak.test {
                skipItems(1) // Skip initial empty state
                val allTimeStreak = awaitItem()

                expectThat(allTimeStreak).isNotNull()
                expectThat(allTimeStreak!!.streakInDays).isEqualTo(45)
                expectThat(allTimeStreak.endDate).isNotNull() // Should be a completed streak
            }
        }

    @Test
    fun `when multiple completed streaks exist then highestAllTimeStreak should return the longest one`() =
        runTest(testDispatcher) {
            // Add multiple completed habit logs with different streak durations
            val today = Date()

            habitLogsRepository.insertHabitLog(
                HabitLog(
                    logId = 1,
                    habitId = 1,
                    streakDuration = 20,
                    createdAt = today,
                ),
            )

            habitLogsRepository.insertHabitLog(
                HabitLog(
                    logId = 2,
                    habitId = 2,
                    streakDuration = 60,
                    createdAt = today,
                ),
            )

            habitLogsRepository.insertHabitLog(
                HabitLog(
                    logId = 3,
                    habitId = 3,
                    streakDuration = 35,
                    createdAt = today,
                ),
            )

            advanceUntilIdle()

            viewModel.highestAllTimeStreak.test {
                skipItems(1) // Skip initial empty state
                val allTimeStreak = awaitItem()

                expectThat(allTimeStreak).isNotNull()
                expectThat(allTimeStreak!!.streakInDays).isEqualTo(60)
                expectThat(allTimeStreak.streak.minDaysRequired).isEqualTo(60) // Should match the Legend streak
            }
        }
}
