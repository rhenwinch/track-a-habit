package io.track.habit.ui.screens.create

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.R
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.repository.HabitRepository
import io.track.habit.domain.utils.StringResource
import io.track.habit.domain.utils.stringLiteral
import io.track.habit.domain.utils.stringRes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreateViewModel
    @Inject
    constructor(
        private val habitRepository: HabitRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val ioScope = CoroutineScope(ioDispatcher)

        private val _uiState = MutableStateFlow(CreateUiState())
        val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

        fun updateHabitName(name: String) {
            _uiState.update { it.copy(habitName = name) }
        }

        fun updateSelectedDate(date: Date) {
            _uiState.update { it.copy(selectedDate = date) }
        }

        private fun validateForm(): Boolean {
            val isNameValid = uiState.value.habitName.isNotBlank()
            _uiState.update { it.copy(isNameError = !isNameValid) }
            return isNameValid
        }

        fun createHabit() {
            ioScope.launch {
                if (validateForm()) {
                    try {
                        val habit =
                            Habit(
                                name = uiState.value.habitName,
                                createdAt = Date(),
                                lastResetAt = uiState.value.selectedDate,
                            )

                        habitRepository.insertHabit(habit)
                        _uiState.update {
                            it.copy(isCreationSuccessful = true)
                        }
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                isCreationSuccessful = false,
                                errorMessage =
                                    e.message?.let { error -> stringLiteral(error) }
                                        ?: stringRes(R.string.error_create_habit),
                            )
                        }
                    }
                }
            }
        }
    }

@Stable
data class CreateUiState(
    val habitName: String = "",
    val selectedDate: Date = Date(),
    val isNameError: Boolean = false,
    val isCreationSuccessful: Boolean = false,
    val errorMessage: StringResource = stringLiteral(""),
)
