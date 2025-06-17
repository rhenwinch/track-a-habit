package io.track.habit.ui.screens.settings.composables.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.track.habit.R
import io.track.habit.ui.theme.TrackAHabitTheme

/**
 * A dialog for editing string settings with a text field.
 *
 * @param title The title of the setting.
 * @param initialValue The initial string value of the setting.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param onConfirm Callback invoked when the user confirms the new value.
 */
@Composable
fun StringSettingDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var textValue by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.settings_dialog_input_label)) },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(textValue) },
            ) {
                Text(text = stringResource(id = R.string.settings_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(text = stringResource(id = R.string.settings_dialog_cancel))
            }
        },
    )
}

@Preview
@Composable
private fun StringSettingDialogPreview() {
    TrackAHabitTheme {
        Surface {
            StringSettingDialog(
                title = "Username",
                initialValue = "John Doe",
                onDismiss = {},
                onConfirm = {},
            )
        }
    }
}
