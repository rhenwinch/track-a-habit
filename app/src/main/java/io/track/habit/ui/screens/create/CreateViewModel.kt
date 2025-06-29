package io.track.habit.ui.screens.create

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.utils.StringResource
import io.track.habit.domain.utils.stringLiteral
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import javax.inject.Inject

/*
* No need to create a test for this ViewModel as it is a simple form handling ViewModel.
* */
@HiltViewModel
class CreateViewModel
    @Inject
    constructor() : ViewModel() {
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

        fun getCreatedHabit(): Habit? {
            return if (validateForm()) {
                Habit(
                    name = uiState.value.habitName,
                    createdAt = Date(),
                    lastResetAt = uiState.value.selectedDate,
                )
            } else {
                null
            }
        }
    }

@Stable
data class CreateUiState(
    val habitName: String = "",
    val selectedDate: Date = Date(),
    val isNameError: Boolean = false,
    val errorMessage: StringResource = stringLiteral(""),
)
