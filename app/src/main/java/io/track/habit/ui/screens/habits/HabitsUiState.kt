package io.track.habit.ui.screens.habits

import androidx.compose.runtime.Stable
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.model.Quote
import io.track.habit.domain.utils.SortOrder

@Stable
data class HabitsUiState(
    val quote: Quote,
    val sortOrder: SortOrder = SortOrder.Streak(),
    val habitIdToShow: Long = 0,
    val longPressedHabit: HabitWithStreak? = null,
    val isCensoringHabitNames: Boolean = false,
    val isInitialized: Boolean = false,
)
