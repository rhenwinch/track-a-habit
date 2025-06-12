package io.track.habit.ui.screens.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.track.habit.ui.theme.TrackAHabitTheme

/**
 * A setting item for boolean values using a Material 3 toggle switch.
 *
 * @param title The title of the setting.
 * @param description Optional description of the setting.
 * @param currentValue The current boolean value of the setting.
 * @param onValueChange Callback invoked when the toggle switch is changed.
 * @param modifier Modifier to be applied to the setting item.
 */
@Composable
fun BooleanSettingItem(
    title: String,
    description: String?,
    currentValue: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
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
            }

            Switch(
                checked = currentValue,
                onCheckedChange = onValueChange,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BooleanSettingItemPreview() {
    TrackAHabitTheme {
        Surface {
            BooleanSettingItem(
                title = "Enable Notifications",
                description = "Receive notifications for your habit reminders",
                currentValue = true,
                onValueChange = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BooleanSettingItemNoDescriptionPreview() {
    TrackAHabitTheme {
        Surface {
            BooleanSettingItem(
                title = "Enable Notifications",
                description = null,
                currentValue = false,
                onValueChange = {},
            )
        }
    }
}
