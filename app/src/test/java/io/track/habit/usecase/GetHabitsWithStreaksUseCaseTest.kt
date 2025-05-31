package io.track.habit.usecase

import app.cash.turbine.test
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.repository.StreakRepository
import io.track.habit.domain.usecase.GetHabitsWithStreaksUseCase
import io.track.habit.domain.usecase.GetStreaksByDaysUseCase
import io.track.habit.repository.fake.FakeHabitRepository
import io.track.habit.repository.fake.FakeStreakRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

class GetHabitsWithStreaksUseCaseTest {
    private lateinit var habitRepository: HabitRepository
    private lateinit var streakRepository: StreakRepository
    private lateinit var getHabitsWithStreaksUseCase: GetHabitsWithStreaksUseCase
    private lateinit var getStreaksByDaysUseCase: GetStreaksByDaysUseCase

    @Before
    fun setup() {
        habitRepository = FakeHabitRepository()
        streakRepository = FakeStreakRepository()
        getStreaksByDaysUseCase = GetStreaksByDaysUseCase(streakRepository)
        getHabitsWithStreaksUseCase = GetHabitsWithStreaksUseCase(habitRepository, getStreaksByDaysUseCase)
    }

    @Test
    fun `getHabitsWithStreaksUseCase returns correct habits with streaks`() =
        runTest {
            val habits =
                List(5) { index ->
                    Habit(
                        habitId = index + 1L,
                        name = "Habit $index",
                        createdAt = Date(),
                        lastResetAt =
                            Date().apply {
                                time -= (index + 3) * 1000 * 60 * 60 * 24
                            },
                    )
                }

            habits.forEach {
                habitRepository.insertHabit(it)
            }

            getHabitsWithStreaksUseCase().test {
                val habitsWithStreaks = awaitItem()

                assert(habitsWithStreaks.size == 5)
                assert(habitsWithStreaks[0].habit.streakInDays == 3)
                assert(habitsWithStreaks[1].habit.streakInDays == 4)
                assert(habitsWithStreaks[2].habit.streakInDays == 5)
                assert(habitsWithStreaks[3].habit.streakInDays == 6)
                assert(habitsWithStreaks[4].habit.streakInDays == 7)
            }
        }
}
