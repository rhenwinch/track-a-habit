package io.track.habit.ui.screens.settings.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.track.habit.ui.screens.settings.composables.dialog.StringSettingDialog
import io.track.habit.ui.theme.TrackAHabitTheme

/**
 * A setting item for string values that opens a dialog with a text field when clicked.
 *
 * @param title The title of the setting.
 * @param description Optional description of the setting.
 * @param currentValue The current string value of the setting.
 * @param onValueChange Callback invoked when the string value is changed.
 * @param modifier Modifier to be applied to the setting item.
 */
@Composable
fun StringSettingItem(
    title: String,
    description: String?,
    currentValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { showDialog = true },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Text(
                text = currentValue.ifEmpty { "Not set" },
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (currentValue.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    if (showDialog) {
        StringSettingDialog(
            title = title,
            initialValue = currentValue,
            onDismiss = { showDialog = false },
            onConfirm = {
                onValueChange(it)
                showDialog = false
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StringSettingItemPreview() {
    TrackAHabitTheme {
        Surface {
            StringSettingItem(
                title = "Username",
                description = "Enter your display name",
                currentValue = "John Doe",
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StringSettingItemEmptyPreview() {
    TrackAHabitTheme {
        Surface {
            StringSettingItem(
                title = "Username",
                description = null,
                currentValue = "",
                onValueChange = {},
            )
        }
    }
}
