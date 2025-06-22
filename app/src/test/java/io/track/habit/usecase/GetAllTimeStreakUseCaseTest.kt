package io.track.habit.usecase

import app.cash.turbine.test
import io.track.habit.R
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.data.repository.StreakRepository
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.usecase.GetAllTimeStreakUseCase
import io.track.habit.domain.usecase.GetHabitsWithStreaksUseCase
import io.track.habit.domain.usecase.GetStreakUseCase
import io.track.habit.domain.utils.StringResource
import io.track.habit.repository.fake.FakeHabitLogsRepository
import io.track.habit.repository.fake.FakeHabitRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.util.Date

class GetAllTimeStreakUseCaseTest {
    private lateinit var getStreakUseCase: GetStreakUseCase
    private lateinit var getHabitsWithStreaksUseCase: GetHabitsWithStreaksUseCase
    private lateinit var habitLogsRepository: HabitLogsRepository
    private lateinit var habitRepository: HabitRepository

    private lateinit var getAllTimeStreakUseCase: GetAllTimeStreakUseCase

    private val testDate = Date()

    @Before
    fun setup() {
        val streakRepository = StreakRepository()
        habitRepository = FakeHabitRepository()
        habitLogsRepository = FakeHabitLogsRepository()

        getStreakUseCase = GetStreakUseCase(streakRepository)
        getHabitsWithStreaksUseCase =
            GetHabitsWithStreaksUseCase(
                habitRepository = habitRepository,
                getStreakByDaysUseCase = getStreakUseCase,
            )

        getAllTimeStreakUseCase =
            GetAllTimeStreakUseCase(
                getStreakByDaysUseCase = getStreakUseCase,
                getHabitsWithStreaksUseCase = getHabitsWithStreaksUseCase,
                habitLogsRepository = habitLogsRepository,
            )
    }

    @Test
    fun `given no habits when fetching all-time streak then returns null`() =
        runTest {
            getAllTimeStreakUseCase().test {
                expectThat(awaitItem()).isNull()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given only ongoing habits then returns highest ongoing streak`() =
        runTest {
            val habit1 = createHabit(1, "Reading", 7)
            val habit2 = createHabit(2, "Meditation", 3)

            habitRepository.insertHabit(habit1)
            habitRepository.insertHabit(habit2)

            getAllTimeStreakUseCase().test {
                val result = awaitItem()
                expectThat(result).isNotNull()
                expectThat(result!!.streakInDays).isEqualTo(7)
                expectThat(
                    (result.streak.title as StringResource.Resource).id,
                ).isEqualTo(R.string.streak_item_week_warrior_title)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given only completed habits then returns highest completed streak`() =
        runTest {
            val completedHabit1 = createHabitLog(1, 20)
            val completedHabit2 = createHabitLog(2, 5)

            habitLogsRepository.insertHabitLog(completedHabit1)
            habitLogsRepository.insertHabitLog(completedHabit2)

            getAllTimeStreakUseCase().test {
                val result = awaitItem()
                expectThat(result).isNotNull()
                expectThat(result!!.streakInDays).isEqualTo(20)
                expectThat(
                    (result.streak.title as StringResource.Resource).id,
                ).isEqualTo(R.string.streak_item_fortnight_fighter_title)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given ongoing habit with higher streak than completed then returns ongoing habit`() =
        runTest {
            val ongoingHabit = createHabit(1, "Running", 40)
            val completedHabit = createHabitLog(2, 20)

            habitRepository.insertHabit(ongoingHabit)
            habitLogsRepository.insertHabitLog(completedHabit)

            getAllTimeStreakUseCase().test {
                val result = awaitItem()
                expectThat(result).isNotNull()
                expectThat(result!!.streakInDays).isEqualTo(40)
                expectThat(
                    (result.streak.title as StringResource.Resource).id,
                ).isEqualTo(R.string.streak_item_monthly_master_title)
                expectThat(result.endDate).isNull()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given completed habit with higher streak than ongoing then returns completed habit`() =
        runTest {
            val ongoingHabit = createHabit(1, "Running", 7)
            val completedHabit = createHabitLog(2, 40)

            habitRepository.insertHabit(ongoingHabit)
            habitLogsRepository.insertHabitLog(completedHabit)

            getAllTimeStreakUseCase().test {
                val result = awaitItem()
                expectThat(result).isNotNull()
                expectThat(result!!.streakInDays).isEqualTo(40)
                expectThat(
                    (result.streak.title as StringResource.Resource).id,
                ).isEqualTo(R.string.streak_item_monthly_master_title)
                expectThat(result.endDate).isNotNull()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given multiple completed habits then returns highest streak`() =
        runTest {
            val completedHabit1 = createHabitLog(1, 7)
            val completedHabit2 = createHabitLog(2, 20)
            val completedHabit3 = createHabitLog(3, 5)

            habitLogsRepository.insertHabitLog(completedHabit1)
            habitLogsRepository.insertHabitLog(completedHabit2)
            habitLogsRepository.insertHabitLog(completedHabit3)

            getAllTimeStreakUseCase().test {
                val result = awaitItem()
                expectThat(result).isNotNull()
                expectThat(result!!.streakInDays).isEqualTo(20)
                expectThat(
                    (result.streak.title as StringResource.Resource).id,
                ).isEqualTo(R.string.streak_item_fortnight_fighter_title)
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun createHabit(
        id: Long,
        name: String,
        streakDays: Int,
    ): Habit {
        // Calculate the lastResetAt date to achieve the desired streak days
        val calendar = java.util.Calendar.getInstance()
        calendar.time = testDate
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -streakDays)
        val calculatedResetDate = calendar.time

        return Habit(
            habitId = id,
            name = name,
            lastResetAt = calculatedResetDate,
        )
    }

    private fun createHabitLog(
        id: Long,
        streakDuration: Int,
    ): HabitLog {
        return HabitLog(
            logId = id,
            habitId = id,
            streakDuration = streakDuration,
            createdAt = testDate,
        )
    }
}
