package io.track.habit.ui.screens.habits

import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.model.Quote
import io.track.habit.domain.utils.SortOrder

data class HabitsUiState(
    val sortOrder: SortOrder = SortOrder.Streak(),
    val quote: Quote? = null,
    val showcasedHabit: HabitWithStreak? = null,
    val selectedHabits: List<HabitWithStreak> = emptyList(),
    val isShowingDeleteConfirmation: Boolean = false,
    val isShowingAddDialog: Boolean = false,
    val isShowingEditDialog: Boolean = false,
)
