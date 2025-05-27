package io.track.habit.domain.model

data class Streak(
    val title: String,
    val minDaysRequired: Int,
    val maxDaysRequired: Int,
    val badgeIcon: String,
    val message: String
)
