package io.track.habit.usecase

import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.usecase.GetCurrentStreakInDaysUseCase
import io.track.habit.repository.fake.FakeHabitLogsRepository
import io.track.habit.repository.fake.FakeHabitRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

class GetCurrentStreakInDaysUseCaseTest {
    private lateinit var useCase: GetCurrentStreakInDaysUseCase
    private lateinit var habitLogsRepository: HabitLogsRepository
    private lateinit var habitRepository: HabitRepository

    @Before
    fun setUp() {
        habitLogsRepository = FakeHabitLogsRepository()
        habitRepository = FakeHabitRepository()
        useCase = GetCurrentStreakInDaysUseCase(habitRepository, habitLogsRepository)
    }

    @Test
    fun `invoke should return 0 when habit has no logs`() =
        runTest {
            val habitId = 1L
            val habit =
                Habit(
                    habitId = habitId,
                    name = "Test Habit",
                )

            habitRepository.insertHabit(habit)

            val result = useCase(habitId)
            assert(result.data == 0)
        }

    @Test
    fun `invoke should return correct streak duration when habit has logs`() =
        runTest {
            val habitId = 1L
            val habit = Habit(habitId = habitId, name = "Test Habit")
            habitRepository.insertHabit(habit)

            val dateTodayMinus3Days = Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000)
            val log = HabitLog(habitId = habitId, streakDuration = 7, createdAt = dateTodayMinus3Days)
            habitLogsRepository.insertHabitLog(log)

            val result = useCase(habitId)
            assert(result.data == 3)
        }
}
