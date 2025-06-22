package io.track.habit.domain.usecase

import io.track.habit.data.repository.StreakRepository
import io.track.habit.domain.model.Streak
import javax.inject.Inject

class GetStreakUseCase
    @Inject
    constructor(
        private val streakRepository: StreakRepository,
    ) {
        operator fun invoke(days: Int): Streak {
            val streaks = streakRepository.getAllStreaks()

            return streaks.find { days >= it.minDaysRequired && days <= it.maxDaysRequired }
                ?: throw IllegalArgumentException("No streak found for $days days")
        }
    }
