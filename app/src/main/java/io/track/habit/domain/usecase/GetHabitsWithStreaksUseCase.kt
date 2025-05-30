package io.track.habit.domain.usecase

import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.repository.StreakRepository
import io.track.habit.domain.utils.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class GetHabitsWithStreaksUseCase
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        private val streakRepository: StreakRepository,
    ) {
        @OptIn(ExperimentalCoroutinesApi::class)
        operator fun invoke(sortOrder: SortOrder = SortOrder.Streak()): Flow<List<HabitWithStreak>> {
            return habitRepository.getAllHabits(sortOrder).mapLatest {
                it.map { habit ->
                    val streak = streakRepository.getStreakByDaysRequired(habit.streakInDays)

                    HabitWithStreak(
                        habit = habit,
                        streak = streak,
                    )
                }
            }
        }
    }
