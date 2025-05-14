package io.track.habit.domain.model

data class Streak(
    val title: String,
    val daysRequired: Int,
    val badgeIcon: String,
    val message: String
)
