package io.track.habit.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.track.habit.R
import io.track.habit.ui.theme.TrackAHabitTheme

@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
) {
    AlertDialog(
        modifier = modifier,
        icon = icon,
        title = { Text(text = dialogTitle) },
        text = { Text(text = dialogText) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmation) {
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

@Preview
@Composable
private fun AlertDialogPreview() {
    TrackAHabitTheme {
        Surface {
            AlertDialog(
                onDismissRequest = {},
                onConfirmation = {},
                dialogTitle = stringResource(R.string.delete_habit),
                dialogText = stringResource(R.string.delete_habit_confirmation),
                icon = { /* Icon can be added here */ },
            )
        }
    }
}
