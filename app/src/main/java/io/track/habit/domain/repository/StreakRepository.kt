package io.track.habit.domain.repository

import io.track.habit.domain.model.Streak

interface StreakRepository {
    fun getAllStreaks(): List<Streak>
}
