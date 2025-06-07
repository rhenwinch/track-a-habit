package io.track.habit.ui.screens.create

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.track.habit.R
import io.track.habit.ui.theme.TrackAHabitTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CreateScreen(
    viewModel: CreateViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    CreateScreenContent(
        habitName = uiState.habitName,
        selectedDate = uiState.selectedDate,
        isNameError = uiState.isNameError,
        onHabitNameChange = viewModel::updateHabitName,
        onDateSelect = viewModel::updateSelectedDate,
        onNavigateBack = onNavigateBack,
        onCreateHabit = {
            viewModel.createHabit()
            if (uiState.isCreationSuccessful) {
                onNavigateBack()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateScreenContent(
    habitName: String,
    selectedDate: Date,
    isNameError: Boolean,
    onHabitNameChange: (String) -> Unit,
    onDateSelect: (Date) -> Unit,
    onCreateHabit: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_new_habit)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.habit_name_privacy_message),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                style = MaterialTheme.typography.bodySmall,
            )

            OutlinedTextField(
                value = habitName,
                onValueChange = onHabitNameChange,
                label = { Text(stringResource(R.string.habit_name)) },
                placeholder = { Text(stringResource(R.string.placeholder_habit_name)) },
                isError = isNameError,
                supportingText =
                    if (isNameError) {
                        { Text(stringResource(R.string.error_empty_habit_name)) }
                    } else {
                        null
                    },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.habit_start_time_explanation),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DatePickerField(
                    selectedDate = selectedDate,
                    onDateSelect = onDateSelect,
                    modifier = Modifier.weight(1f),
                )

                TimePickerField(
                    selectedDate = selectedDate,
                    onTimeSelect = { newDate -> onDateSelect(newDate) },
                    modifier = Modifier.weight(1f),
                )
            }

            Button(
                onClick = onCreateHabit,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.add_habit),
                )

                Text(
                    text = stringResource(R.string.add_habit),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    selectedDate: Date,
    onDateSelect: (Date) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release) {
                showDatePicker = true
            }
        }
    }

    OutlinedTextField(
        value = dateFormatter.format(selectedDate),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.date)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.date_icon_content_desc),
            )
        },
        modifier = modifier,
        interactionSource = interactionSource,
    )

    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            // Create new date while preserving the time
                            val calendar = Calendar.getInstance()
                            calendar.time = selectedDate
                            val hours = calendar.get(Calendar.HOUR_OF_DAY)
                            val minutes = calendar.get(Calendar.MINUTE)

                            calendar.timeInMillis = it
                            calendar.set(Calendar.HOUR_OF_DAY, hours)
                            calendar.set(Calendar.MINUTE, minutes)

                            onDateSelect(calendar.time)
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerField(
    selectedDate: Date,
    onTimeSelect: (Date) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release) {
                showTimePicker = true
            }
        }
    }

    val calendar = remember { Calendar.getInstance() }
    calendar.time = selectedDate

    OutlinedTextField(
        value = timeFormatter.format(selectedDate),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.time)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.time_icon_content_desc),
            )
        },
        modifier = modifier,
        interactionSource = interactionSource,
    )

    if (showTimePicker) {
        val timePickerState =
            rememberTimePickerState(
                initialHour = calendar.get(Calendar.HOUR_OF_DAY),
                initialMinute = calendar.get(Calendar.MINUTE),
                is24Hour = false,
            )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Create new date with updated time
                        val newCalendar = Calendar.getInstance()
                        newCalendar.time = selectedDate
                        newCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        newCalendar.set(Calendar.MINUTE, timePickerState.minute)

                        onTimeSelect(newCalendar.time)
                        showTimePicker = false
                    },
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateScreenPreview() {
    TrackAHabitTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CreateScreenContent(
                habitName = "Morning Exercise",
                selectedDate = Date(),
                isNameError = false,
                onHabitNameChange = {},
                onDateSelect = {},
                onCreateHabit = {},
                onNavigateBack = {},
            )
        }
    }
}
