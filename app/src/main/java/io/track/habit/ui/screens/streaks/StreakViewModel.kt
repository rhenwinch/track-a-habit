@file:OptIn(ExperimentalCoroutinesApi::class)

package io.track.habit.ui.screens.streaks

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.R
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.model.Streak
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.repository.StreakRepository
import io.track.habit.domain.usecase.GetAllTimeStreakUseCase
import io.track.habit.domain.usecase.GetHabitsWithStreaksUseCase
import io.track.habit.domain.utils.SortOrder
import io.track.habit.domain.utils.StringResource
import io.track.habit.domain.utils.asStateFlow
import io.track.habit.domain.utils.pluralStringRes
import io.track.habit.domain.utils.stringLiteral
import io.track.habit.domain.utils.stringRes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.text.NumberFormat
import javax.inject.Inject

private const val PERCENTAGE_THRESHOLD = 0.95f // 95% threshold for revealing streak details

@HiltViewModel
class StreakViewModel
    @Inject
    constructor(
        streakRepository: StreakRepository,
        habitRepository: HabitRepository,
        habitLogsRepository: HabitLogsRepository,
        getHabitsWithStreaksUseCase: GetHabitsWithStreaksUseCase,
        getAllTimeStreakUseCase: GetAllTimeStreakUseCase,
    ) : ViewModel() {
        private val streaks = streakRepository.getAllStreaks()
        private val longestStreakInDays =
            combine(
                habitRepository.getLongestStreakInDays(),
                habitLogsRepository.getLongestStreakInDays(),
            ) { habitStreak, habitLogStreak ->
                maxOf(habitStreak, habitLogStreak)
            }.distinctUntilChanged()

        private val habitsFlow =
            habitRepository
                .getAllHabits(SortOrder.Streak(ascending = false))

        val streakSummaries =
            combine(longestStreakInDays, habitsFlow) { longestStreak, habits ->
                streaks.map { streak ->
                    mapToStreakSummary(streak, longestStreak, habits)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

        val highestAllTimeStreak =
            getAllTimeStreakUseCase()
                .asStateFlow(viewModelScope, initialValue = null)

        val highestOngoingStreak =
            getHabitsWithStreaksUseCase(
                sortOrder = SortOrder.Streak(ascending = false),
            ).mapLatest {
                val item = it.firstOrNull()

                if (item?.habit?.streakInDays == 0) {
                    null
                } else {
                    item
                }
            }.asStateFlow(viewModelScope, initialValue = null)

        private fun mapToStreakSummary(
            streak: Streak,
            longestStreak: Int,
            habits: List<Habit>,
        ): StreakSummary {
            val isAchieved = longestStreak >= streak.minDaysRequired

            val title =
                if (isAchieved) {
                    stringLiteral(streak.title)
                } else {
                    stringRes(R.string.streak_mystery_title)
                }

            // Count habits within milestone range
            val habitsInRange =
                habits.count { habit ->
                    habit.streakInDays >= streak.minDaysRequired &&
                        (
                            streak.maxDaysRequired == Int.MAX_VALUE ||
                                habit.streakInDays <= streak.maxDaysRequired
                        )
                }

            val status =
                when {
                    isAchieved -> pluralStringRes(R.plurals.streak_achieved_habits, habitsInRange, habitsInRange)
                    longestStreak >= (streak.minDaysRequired * PERCENTAGE_THRESHOLD).toInt() ->
                        stringRes(R.string.streak_very_close)

                    else -> stringRes(R.string.streak_not_achieved)
                }

            return StreakSummary(
                title = title,
                status = status,
                durationText = getDurationText(streak, longestStreak),
                isAchieved = isAchieved,
//                badgeIcon = streak.badgeIcon,
                badgeIcon = "habit_logs", // TODO: Placeholder for badge icon, replace with actual icon logic
            )
        }

        private fun getDurationText(
            streak: Streak,
            longestStreak: Int,
        ): StringResource {
            return when {
                // User has fully achieved this milestone and gone beyond it
                longestStreak > streak.maxDaysRequired -> {
                    val minDays = NumberFormat.getNumberInstance().format(streak.minDaysRequired)
                    val maxDays = NumberFormat.getNumberInstance().format(streak.maxDaysRequired)
                    stringRes(R.string.streak_days_range, minDays, maxDays)
                }
                // User is progressing but hasn't completed this milestone yet
                longestStreak >= streak.minDaysRequired -> {
                    val minDays = NumberFormat.getNumberInstance().format(streak.minDaysRequired)
                    stringRes(R.string.streak_partial_days_range, minDays)
                }

                else -> stringRes(R.string.streak_unknown_days)
            }
        }
    }

@Stable
data class StreakSummary(
    val title: StringResource,
    val status: StringResource,
    val durationText: StringResource,
    val isAchieved: Boolean,
    val badgeIcon: String, // New property for streak badge icon
)
