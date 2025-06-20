package io.track.habit.ui.screens.habits

import androidx.compose.ui.util.fastFirstOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.data.local.datastore.entities.UserAppStateRegistry
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.repository.HabitLogsRepository
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.usecase.GetHabitsWithStreaksUseCase
import io.track.habit.domain.usecase.GetRandomQuoteUseCase
import io.track.habit.domain.utils.SortOrder
import io.track.habit.domain.utils.asStateFlow
import io.track.habit.ui.screens.habits.composables.ResetDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HabitsViewModel
    @Inject
    constructor(
        getHabitsWithStreaksUseCase: GetHabitsWithStreaksUseCase,
        getRandomQuoteUseCase: GetRandomQuoteUseCase,
        private val settingsDataStore: SettingsDataStore,
        private val habitRepository: HabitRepository,
        private val habitLogsRepository: HabitLogsRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val ioScope = CoroutineScope(ioDispatcher)

        private val _uiState = MutableStateFlow(HabitsUiState(quote = getRandomQuoteUseCase()))
        val uiState = _uiState.asStateFlow()

        private var updateJob: Job? = null
        private var deleteJob: Job? = null
        private var resetProgressJob: Job? = null
        private var changeShowcaseJob: Job? = null

        val username = settingsDataStore
            .generalSettingsFlow
            .map { it.userName }
            .distinctUntilChanged()
            .asStateFlow(
                scope = viewModelScope,
                initialValue = "",
            )

        val isResetProgressButtonLocked =
            settingsDataStore
                .generalSettingsFlow
                .map { it.lockResetProgressButton }
                .distinctUntilChanged()
                .asStateFlow(
                    scope = viewModelScope,
                    initialValue = false,
                )

        val habits = uiState
            .map { it.sortOrder }
            .distinctUntilChanged()
            .flatMapLatest { sortOrder ->
                getHabitsWithStreaksUseCase(sortOrder = sortOrder).map { habits ->
                    updateShowcasedHabit(habits)
                    habits
                }
            }.asStateFlow(
                scope = viewModelScope,
                initialValue = emptyList(),
            )

        init {
            viewModelScope.launch {
                launch {
                    val generalSettings =
                        settingsDataStore
                            .generalSettingsFlow
                            .first()
                    val appState =
                        settingsDataStore
                            .appStateFlow
                            .first()

                    val initialCensorSetting = generalSettings.censorHabitNames
                    val initialShowcasedHabitId = appState.lastShowcasedHabitId

                    val habits = getHabitsWithStreaksUseCase().first()
                    val habit =
                        habits.fastFirstOrNull { it.habit.habitId == initialShowcasedHabitId }
                            ?: habits.firstOrNull()

                    _uiState.update {
                        it.copy(
                            isCensoringHabitNames = initialCensorSetting,
                            habitToShowcase = habit,
                            isInitialized = true,
                        )
                    }
                }

                launch {
                    settingsDataStore.generalSettingsFlow
                        .map { it.censorHabitNames }
                        .distinctUntilChanged()
                        .collectLatest {
                            toggleCensorshipOnNames(it)
                        }
                }

                launch {
                    habits.collectLatest {
                        toggleShowcaseHabit(it.firstOrNull())
                    }
                }
            }
        }

        private fun updateShowcasedHabit(habits: List<HabitWithStreak>) {
            _uiState.update { currentState ->
                val updatedShowcaseHabit =
                    habits.firstOrNull { habit ->
                        habit.habit.habitId == currentState.habitToShowcase?.habit?.habitId
                    }
                currentState.copy(habitToShowcase = updatedShowcaseHabit)
            }
        }

        fun updateHabit(habit: Habit) {
            if (updateJob?.isActive == true) return

            updateJob =
                ioScope.launch {
                    habitRepository.updateHabit(habit)
                }
        }

        fun deleteHabit(habit: Habit) {
            if (deleteJob?.isActive == true) return

            deleteJob =
                ioScope.launch {
                    habitRepository.deleteHabit(habit)
                    habits.value.firstOrNull()?.let(::toggleShowcaseHabit)
                }
        }

        fun resetProgress(resetDetails: ResetDetails) {
            if (resetProgressJob?.isActive == true) return

            resetProgressJob =
                ioScope.launch {
                    val originalHabit = habitRepository.getHabitById(resetDetails.habitId)!!
                    val updatedHabit = originalHabit.copy(lastResetAt = Date())

                    val resetLog =
                        HabitLog(
                            habitId = resetDetails.habitId,
                            streakDuration = originalHabit.streakInDays,
                            trigger = resetDetails.trigger,
                            notes = resetDetails.notes,
                        )

                    habitRepository.updateHabit(updatedHabit)
                    habitLogsRepository.insertHabitLog(resetLog)
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

        fun toggleShowcaseHabit(habitWithStreak: HabitWithStreak?) {
            _uiState.update { it.copy(habitToShowcase = habitWithStreak) }

            if (changeShowcaseJob?.isActive == true) {
                changeShowcaseJob?.cancel()
            }

            changeShowcaseJob =
                ioScope.launch {
                    val currentSettings = settingsDataStore.appStateFlow.first()
                    val id = habitWithStreak?.habit?.habitId
                        ?: UserAppStateRegistry.LAST_SHOWCASED_HABIT_ID.defaultValue

                    settingsDataStore.updateSettings(currentSettings.copy(lastShowcasedHabitId = id))
                }
        }
    }
