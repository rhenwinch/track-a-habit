package io.track.habit.ui.screens.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingType

/**
 * A composable that renders the appropriate UI for a setting based on its type.
 *
 * @param definition The setting definition containing metadata about the setting.
 * @param currentValue The current value of the setting.
 * @param onValueChange Callback invoked when the setting value changes.
 * @param modifier Modifier to be applied to the setting item.
 */
@Composable
fun <T> SettingItem(
    definition: SettingDefinition<*>,
    currentValue: T,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = definition.displayName.asString()
    val description = definition.description?.asString()

    when (definition.type) {
        is SettingType.StringType -> {
            StringSettingItem(
                title = title,
                description = description,
                currentValue = currentValue as String,
                onValueChange = { onValueChange(it as T) },
                modifier = modifier,
            )
        }

        is SettingType.BooleanType -> {
            BooleanSettingItem(
                title = title,
                description = description,
                currentValue = currentValue as Boolean,
                onValueChange = { onValueChange(it as T) },
                modifier = modifier,
            )
        }
        // Handle other types if needed in the future
        else -> {
            // For unsupported types, we don't render anything
        }
    }
}
