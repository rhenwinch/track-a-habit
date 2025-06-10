@file:OptIn(ExperimentalCoroutinesApi::class)

package io.track.habit.ui.screens.streaks

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.R
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.repository.StreakRepository
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
                    val isAchieved = longestStreak >= streak.minDaysRequired
                    val title =
                        if (isAchieved) {
                            stringLiteral(streak.title)
                        } else {
                            stringRes(R.string.streak_mystery_title)
                        }

                    // Count habits that have a streak within this milestone's range
                    val habitsInRange =
                        habits.count { habit ->
                            habit.streakInDays >= streak.minDaysRequired &&
                                (
                                    streak.maxDaysRequired == Int.MAX_VALUE ||
                                        habit.streakInDays <= streak.maxDaysRequired
                                )
                        }

                    val status =
                        if (isAchieved) {
                            pluralStringRes(R.plurals.streak_achieved_habits, habitsInRange, habitsInRange)
                        } else if (longestStreak >= (streak.minDaysRequired * PERCENTAGE_THRESHOLD).toInt()) {
                            stringRes(R.string.streak_very_close)
                        } else {
                            stringRes(R.string.streak_not_achieved)
                        }

                    val durationText =
                        when {
                            // User has fully achieved this milestone and gone beyond it
                            longestStreak > streak.maxDaysRequired -> {
                                val minDays = NumberFormat.getNumberInstance().format(streak.minDaysRequired)
                                val maxDays = NumberFormat.getNumberInstance().format(streak.maxDaysRequired)
                                stringRes(R.string.streak_days_range, minDays, maxDays)
                            }
                            // User is progressing but hasn't completed this milestone yet
                            // Show the minimum days but keep maximum as "??"
                            longestStreak >= streak.minDaysRequired -> {
                                val minDays = NumberFormat.getNumberInstance().format(streak.minDaysRequired)
                                stringRes(R.string.streak_partial_days_range, minDays)
                            }
                            // User hasn't made enough progress to reveal details
                            else -> {
                                stringRes(R.string.streak_unknown_days)
                            }
                        }

                    StreakSummary(
                        title = title,
                        status = status,
                        durationText = durationText,
                        isAchieved = isAchieved, // Add isAchieved property
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

        // val highestAllTimeStreak = TODO("Implement logic to determine if all-time streak is achieved")

        val highestOngoingStreak =
            getHabitsWithStreaksUseCase(
                sortOrder = SortOrder.Streak(ascending = false),
            ).mapLatest {
                it.firstOrNull()
            }.asStateFlow(viewModelScope, initialValue = null)
    }

@Stable
data class StreakSummary(
    val title: StringResource,
    val status: StringResource,
    val durationText: StringResource,
    val isAchieved: Boolean, // Add isAchieved property
)
