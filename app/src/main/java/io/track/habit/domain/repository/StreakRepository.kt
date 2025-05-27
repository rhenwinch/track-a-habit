package io.track.habit.domain.repository

import io.track.habit.domain.model.Streak

interface StreakRepository {
    suspend fun getAllStreaks(): List<Streak>

    suspend fun getStreakByDaysRequired(days: Int): Streak
}
