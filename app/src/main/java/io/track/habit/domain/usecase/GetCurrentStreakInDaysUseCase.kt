package io.track.habit.domain.usecase

import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.utils.Resource
import kotlinx.coroutines.flow.first
import java.util.Date
import javax.inject.Inject

class GetCurrentStreakInDaysUseCase
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        private val habitLogsRepository: HabitLogsRepository,
    ) {
        suspend operator fun invoke(habitId: Long): Resource<Int> {
            val habitLogs = habitLogsRepository.getHabitLogsByHabitId(habitId).first()
            val currentDate = Date().time

            if (habitLogs.isEmpty()) {
                val habit =
                    habitRepository.getHabitById(habitId)
                        ?: return Resource.Error("Habit not found")

                val startDate = habit.createdAt.time

                val daysSinceStart = (currentDate - startDate) / (1000 * 60 * 60 * 24)
                return Resource.Success(daysSinceStart.toInt())
            }

            val lastLog = habitLogs.first()
            val lastLogDate = lastLog.createdAt.time
            val daysSinceLastLog = (currentDate - lastLogDate) / (1000 * 60 * 60 * 24)

            return Resource.Success(daysSinceLastLog.toInt())
        }
    }
