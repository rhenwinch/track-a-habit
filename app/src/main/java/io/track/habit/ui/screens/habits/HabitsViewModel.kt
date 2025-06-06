package io.track.habit.ui.screens.habits

import androidx.compose.ui.util.fastMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.model.HabitWithStreak.Companion.censorName
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.usecase.GetHabitsWithStreaksUseCase
import io.track.habit.domain.usecase.GetRandomQuoteUseCase
import io.track.habit.domain.utils.SortOrder
import io.track.habit.domain.utils.coroutines.asStateFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class HabitsViewModel
    @Inject
    constructor(
        getHabitsWithStreaksUseCase: GetHabitsWithStreaksUseCase,
        getRandomQuoteUseCase: GetRandomQuoteUseCase,
        private val settingsDataStore: SettingsDataStore,
        private val habitRepository: HabitRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val ioScope = CoroutineScope(ioDispatcher)

        private val _uiState = MutableStateFlow(HabitsUiState(quote = getRandomQuoteUseCase()))
        val uiState = _uiState.asStateFlow()

        private var deleteJob: Job? = null
        private var resetProgressJob: Job? = null
        private var changeShowcaseJob: Job? = null

        init {
            viewModelScope.launch {
                val generalSettings =
                    settingsDataStore
                        .generalSettingsFlow
                        .first()

                val initialCensorSetting = generalSettings.censorHabitNames
                val initialShowcasedHabitId = generalSettings.lastShowcasedHabitId

                val habits = getHabitsWithStreaksUseCase().first()
                val habit = habits.indexOfFirst { it.habit.habitId == initialShowcasedHabitId }.toLong()

                _uiState.update {
                    it.copy(
                        isCensoringHabitNames = initialCensorSetting,
                        habitIdToShow = max(habit, 0L),
                        isInitialized = true,
                    )
                }
            }
        }

        val username by lazy {
            settingsDataStore
                .generalSettingsFlow
                .map { it.userName }
                .distinctUntilChanged()
                .asStateFlow(
                    scope = viewModelScope,
                    initialValue = "",
                )
        }

        val isCensoringHabitNames by lazy {
            uiState
                .map { it.isCensoringHabitNames }
                .distinctUntilChanged()
                .asStateFlow(
                    scope = viewModelScope,
                    initialValue = uiState.value.isCensoringHabitNames,
                )
        }

        val isResetProgressButtonLocked =
            settingsDataStore
                .generalSettingsFlow
                .map { it.lockResetProgressButton }
                .distinctUntilChanged()
                .asStateFlow(
                    scope = viewModelScope,
                    initialValue = false,
                )

        val habits by lazy {
            combine(
                getHabitsWithStreaksUseCase(),
                this@HabitsViewModel.isCensoringHabitNames,
            ) { habits, censorHabitNames ->
                habits.fastMap { it.censorName(censorHabitNames) }
            }.asStateFlow(
                scope = viewModelScope,
                initialValue = emptyList(),
            )
        }

        fun deleteHabit(habit: Habit) {
            if (deleteJob?.isActive == true) return

            deleteJob =
                ioScope.launch {
                    habitRepository.deleteHabit(habit)
                }
        }

        fun resetProgress(habit: Habit) {
            if (resetProgressJob?.isActive == true) return

            resetProgressJob =
                ioScope.launch {
                    val originalHabit = habitRepository.getHabitById(habit.habitId)!!

                    habitRepository.updateHabit(
                        originalHabit.copy(lastResetAt = Date()),
                    )
                }
        }

        fun onHabitLongClick(habit: HabitWithStreak?) {
            _uiState.update {
                it.copy(longPressedHabit = habit)
            }
        }

        fun onSortOrderSelect(sortOrder: SortOrder) {
            _uiState.update { it.copy(sortOrder = sortOrder) }
        }

        fun toggleCensorshipOnNames(show: Boolean) {
            _uiState.update {
                it.copy(isCensoringHabitNames = show)
            }
        }

        fun toggleShowcaseHabit(habit: HabitWithStreak) {
            _uiState.update { it.copy(habitIdToShow = habit.habit.habitId) }

            if (habit != null) {
                if (changeShowcaseJob?.isActive == true) {
                    changeShowcaseJob?.cancel()
                }

                changeShowcaseJob =
                    ioScope.launch {
                        val currentSettings = settingsDataStore.generalSettingsFlow.first()

                        settingsDataStore.updateSettings(
                            currentSettings.copy(lastShowcasedHabitId = habit.habit.habitId),
                        )
                    }
            }
        }
    }
