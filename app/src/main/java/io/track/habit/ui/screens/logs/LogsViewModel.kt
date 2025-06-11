package io.track.habit.ui.screens.logs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.model.Streak
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.usecase.GetStreakUseCase
import io.track.habit.domain.utils.asStateFlow
import io.track.habit.ui.navigation.SubNavRoute
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LogsViewModel
    @Inject
    constructor(
        private val habitLogsRepository: HabitLogsRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        getStreakUseCase: GetStreakUseCase,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        val habitId = savedStateHandle.toRoute<SubNavRoute.HabitsViewLogs>().habitId

        private val scope = CoroutineScope(ioDispatcher + SupervisorJob())
        private var updateJob: Job? = null

        val logs =
            habitLogsRepository
                .getHabitLogsByHabitId(habitId)
                .mapLatest { list ->
                    list.map {
                        val streak =
                            getStreakUseCase(it.streakDuration)
                                .copy(
                                    badgeIcon = "habit_logs", // TODO: Replace with actual badge icon logic
                                )

                        HabitLogWithStreak(
                            streak = streak,
                            log = it,
                        )
                    }
                }.asStateFlow(
                    scope = viewModelScope,
                    initialValue = emptyList(),
                )

        fun updateLog(log: HabitLog) {
            if (updateJob?.isActive == true) return

            updateJob =
                scope.launch {
                    habitLogsRepository.updateHabitLog(log)
                }
        }
    }

data class HabitLogWithStreak(
    val streak: Streak,
    val log: HabitLog,
) {
    val dateDuration: String
        get() {
            val startDate =
                Calendar
                    .getInstance()
                    .apply {
                        time = log.createdAt
                        add(Calendar.DAY_OF_YEAR, log.streakDuration)
                    }.time
            val endDate = log.createdAt

            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val formattedStartDate = dateFormat.format(startDate)
            val formattedEndDate = dateFormat.format(endDate)

            return "$formattedStartDate - $formattedEndDate"
        }
}
