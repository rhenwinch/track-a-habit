package io.track.habit.domain.model

import io.track.habit.domain.utils.DrawableResource
import io.track.habit.domain.utils.StringResource

data class Streak(
    val title: StringResource,
    val minDaysRequired: Int,
    val maxDaysRequired: Int,
    val badgeIcon: DrawableResource,
    val message: StringResource,
)
