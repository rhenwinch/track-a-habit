package io.track.habit.ui.screens.streaks

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.domain.model.Streak
import io.track.habit.domain.utils.StringResource
import javax.inject.Inject

@HiltViewModel
class StreakViewModel
    @Inject
    constructor() : ViewModel()

@Stable
data class StreakSummary(
    val streak: Streak,
    val status: StringResource,
    val durationText: StringResource,
) {
    val name get() = streak.title
}
