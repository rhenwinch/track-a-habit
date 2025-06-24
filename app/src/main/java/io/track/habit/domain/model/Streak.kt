package io.track.habit.domain.model

import androidx.compose.runtime.Immutable
import io.track.habit.domain.utils.DrawableResource
import io.track.habit.domain.utils.StringResource

@Immutable
data class Streak(
    val title: StringResource,
    val minDaysRequired: Int,
    val maxDaysRequired: Int,
    val badgeIcon: DrawableResource,
    val message: StringResource,
)
