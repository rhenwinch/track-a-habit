package io.track.habit.data.repository

import io.track.habit.data.local.database.dao.HabitLogDao
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.data.local.database.entities.HabitWithLogs
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.utils.coroutines.AppDispatcher.Companion.withIOContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class HabitLogsRepositoryImpl
    @Inject
    constructor(
        private val habitLogsDao: HabitLogDao,
    ) : HabitLogsRepository {
        override fun getHabitLogsByHabitId(habitId: Long): Flow<List<HabitLog>> {
            return habitLogsDao.getHabitLogsByHabitId(habitId)
        }

        override fun getLongestStreakForHabit(habitId: Long): Flow<HabitLog?> {
            return habitLogsDao.getLongestStreakForHabit(habitId)
        }

        override suspend fun getHabitLogById(logId: Long): HabitLog? {
            return habitLogsDao.getHabitLogById(logId)
        }

        override suspend fun insertHabitLog(habitLog: HabitLog) =
            withIOContext {
                habitLogsDao.insertHabitLog(habitLog)
            }

        override suspend fun updateHabitLog(habitLog: HabitLog) {
            habitLogsDao.updateHabitLog(habitLog)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs?> {
            return habitLogsDao
                .getHabitWithLogsFlow(habitId)
                .mapLatest {
                    it?.let { habitWithLogs ->
                        habitWithLogs.copy(
                            logs = habitWithLogs.logs.sortedByDescending { log -> log.createdAt },
                        )
                    }
                }
        }
    }
