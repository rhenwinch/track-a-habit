package io.track.habit.domain.usecase

import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.domain.model.AllTimeStreak
import io.track.habit.domain.model.AllTimeStreak.Companion.toAllTimeStreak
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.utils.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAllTimeStreakUseCase
    @Inject
    constructor(
        private val getStreakByDaysUseCase: GetStreakUseCase,
        private val getHabitsWithStreaksUseCase: GetHabitsWithStreaksUseCase,
        private val habitLogsRepository: HabitLogsRepository,
    ) {
        operator fun invoke(): Flow<AllTimeStreak?> {
            return combine(
                getHabitsWithStreaksUseCase(sortOrder = SortOrder.Streak(ascending = false)),
                habitLogsRepository.getHabitLogs(),
                ::determineHighestStreak,
            )
        }

        private fun determineHighestStreak(
            onGoingHabits: List<HabitWithStreak>,
            allTimeHabits: List<HabitLog>,
        ): AllTimeStreak? {
            val highestOngoingStreak = onGoingHabits.firstOrNull()
            val highestAllTimeStreak = allTimeHabits.firstOrNull()

            if (highestOngoingStreak == null && highestAllTimeStreak == null) {
                return null
            }

            if (highestAllTimeStreak?.streakDuration == 0 && highestOngoingStreak?.habit?.streakInDays == 0) {
                return null
            }

            if (highestOngoingStreak == null) {
                val streak = getStreakByDaysUseCase(highestAllTimeStreak!!.streakDuration)
                return highestAllTimeStreak.toAllTimeStreak(streak)
            }

            if (highestAllTimeStreak == null) {
                return highestOngoingStreak.toAllTimeStreak()
            }

            return if (highestOngoingStreak.habit.streakInDays > highestAllTimeStreak.streakDuration) {
                highestOngoingStreak.toAllTimeStreak()
            } else {
                val streak = getStreakByDaysUseCase(highestAllTimeStreak.streakDuration)
                highestAllTimeStreak.toAllTimeStreak(streak)
            }
        }
    }
