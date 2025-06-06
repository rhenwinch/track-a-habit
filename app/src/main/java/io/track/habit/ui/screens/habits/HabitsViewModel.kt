package io.track.habit.ui.screens.habits

import androidx.activity.result.launch
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
import io.track.habit.domain.utils.coroutines.AppDispatcher.Companion.withIOContext
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
import javax.inject.Inject

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

        var deleteJob: Job? = null
        var addJob: Job? = null
        var editJob: Job? = null

        init {
            viewModelScope.launch {
                val initialCensorSetting =
                    settingsDataStore
                        .generalSettingsFlow
                        .first()
                        .censorHabitNames

                toggleCensorshipOnNames(initialCensorSetting)
            }
        }

        val isCensoringHabitNames =
            uiState
                .map { it.isCensoringHabitNames }
                .distinctUntilChanged()
                .asStateFlow(
                    scope = viewModelScope,
                    initialValue = uiState.value.isCensoringHabitNames,
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

        val habits =
            combine(
                getHabitsWithStreaksUseCase(),
                this@HabitsViewModel.isCensoringHabitNames,
            ) { habits, censorHabitNames ->
                habits.fastMap { it.censorName(censorHabitNames) }
            }.asStateFlow(
                scope = viewModelScope,
                initialValue = emptyList(),
            )

        suspend fun deleteHabit(habit: Habit) =
            withIOContext {
                habitRepository.deleteHabit(habit)
            }

        fun deleteSelectedHabits() {
            if (deleteJob?.isActive == true) return

            deleteJob =
                ioScope.launch {
                    _uiState.value.selectedHabits.forEach {
                        deleteHabit(it.habit)
                    }
                    unselectAll()
                }
        }

        fun addHabit(habit: Habit) {
            if (addJob?.isActive == true) return

            addJob =
                ioScope.launch {
                    habitRepository.insertHabit(habit)
                }
        }

        fun updateHabit(habit: Habit) {
            if (editJob?.isActive == true) return

            editJob = ioScope.launch { habitRepository.updateHabit(habit) }
        }

        fun toggleSelectionOnHabit(habit: HabitWithStreak) {
            _uiState.update {
                val selectedHabits = it.selectedHabits.toMutableList()

                if (selectedHabits.contains(habit)) {
                    selectedHabits.remove(habit)
                } else {
                    selectedHabits.add(habit)
                }

                it.copy(selectedHabits = selectedHabits.toList())
            }
        }

        fun unselectAll() {
            _uiState.update { it.copy(selectedHabits = emptyList()) }
        }

        fun toggleSortOrder(sortOrder: SortOrder) {
            _uiState.update { it.copy(sortOrder = sortOrder) }
        }

        fun toggleDeleteConfirmation(show: Boolean) {
            _uiState.update { it.copy(isShowingDeleteConfirmation = show) }
        }

        fun toggleAddDialog(show: Boolean) {
            _uiState.update { it.copy(isShowingAddDialog = show) }
        }

        fun toggleEditDialog(show: Boolean) {
            _uiState.update { it.copy(isShowingEditDialog = show) }
        }

        fun toggleCensorshipOnNames(show: Boolean) {
            _uiState.update {
                it.copy(isCensoringHabitNames = show)
            }
        }
    }
