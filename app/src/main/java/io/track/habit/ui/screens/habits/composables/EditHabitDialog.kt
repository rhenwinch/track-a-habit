package io.track.habit.ui.screens.habits.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.track.habit.R
import io.track.habit.ui.theme.TrackAHabitTheme

@Composable
fun EditHabitDialog(
    initialHabitName: String,
    onDismissRequest: () -> Unit = {},
    onSaveClick: (String) -> Unit = {},
) {
    EditHabitDialogContent(
        initialHabitName = initialHabitName,
        onDismissRequest = onDismissRequest,
        onSaveClick = onSaveClick,
    )
}

@Composable
private fun EditHabitDialogContent(
    initialHabitName: String,
    onDismissRequest: () -> Unit,
    onSaveClick: (String) -> Unit,
) {
    var habitName by rememberSaveable { mutableStateOf(initialHabitName) }
    var isError by remember { mutableStateOf(false) }

    // Reset the error state when habitName changes
    LaunchedEffect(habitName) {
        if (isError && habitName.isNotEmpty()) {
            isError = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.edit_habit)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text(stringResource(R.string.habit_name)) },
                    isError = isError,
                    supportingText =
                        if (isError) {
                            { Text(stringResource(R.string.error_empty_habit_name)) }
                        } else {
                            null
                        },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitName.isEmpty()) {
                        isError = true
                    } else {
                        onSaveClick(habitName)
                    }
                },
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun EditHabitDialogPreview() {
    TrackAHabitTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            EditHabitDialogContent(
                initialHabitName = "Morning Run",
                onDismissRequest = {},
                onSaveClick = {},
            )
        }
    }
}
