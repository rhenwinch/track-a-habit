package io.track.habit.ui.screens.habits.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.track.habit.R
import io.track.habit.ui.theme.TrackAHabitTheme

data class ResetDetails(
    val habitId: Long,
    val trigger: String? = null,
    val notes: String? = null,
)

@Composable
fun ResetProgressDialog(
    habitId: Long,
    onDismissRequest: () -> Unit,
    onConfirm: (ResetDetails) -> Unit,
) {
    var trigger by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.reset_progress)) },
        text = {
            ResetProgressDialogContent(
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
                        ResetDetails(
                            habitId = habitId,
                            trigger = trigger.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() },
                        ),
                    )
                },
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
    )
}

@Composable
private fun ResetProgressDialogContent(
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
        WarningMessage()

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

@Composable
private fun WarningMessage(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
    ) {
        Text(
            text = stringResource(R.string.reset_progress_confirmation),
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResetProgressDialogPreview() {
    TrackAHabitTheme {
        ResetProgressDialogContent(
            trigger = "",
            onTriggerChange = {},
            notes = "",
            onNotesChange = {},
        )
    }
}
