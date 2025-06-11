package io.track.habit.ui.screens.logs.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.track.habit.R
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.ui.theme.TrackAHabitTheme

@Composable
fun EditLogDialog(
    habitLog: HabitLog,
    onDismissRequest: () -> Unit,
    onConfirm: (HabitLog) -> Unit,
) {
    var trigger by rememberSaveable { mutableStateOf(habitLog.trigger ?: "") }
    var notes by rememberSaveable { mutableStateOf(habitLog.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.edit_log)) },
        text = {
            EditLogDialogContent(
                trigger = trigger,
                onTriggerChange = { trigger = it },
                notes = notes,
                onNotesChange = { notes = it },
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        habitLog.copy(
                            trigger = trigger.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() },
                        ),
                    )
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

@Composable
private fun EditLogDialogContent(
    trigger: String,
    onTriggerChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        OutlinedTextField(
            value = trigger,
            onValueChange = onTriggerChange,
            label = { Text(stringResource(R.string.placeholder_trigger_cause)) },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.placeholder_notes)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditLogDialogPreview() {
    TrackAHabitTheme {
        EditLogDialogContent(
            trigger = "Example trigger",
            onTriggerChange = {},
            notes = "Example notes for the log entry.",
            onNotesChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyEditLogDialogPreview() {
    TrackAHabitTheme {
        EditLogDialogContent(
            trigger = "",
            onTriggerChange = {},
            notes = "",
            onNotesChange = {},
        )
    }
}
