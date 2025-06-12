package io.track.habit.domain.datastore

import androidx.compose.runtime.Immutable
import io.track.habit.domain.utils.StringResource
import io.track.habit.domain.utils.stringLiteral

/**
 * Represents the definition of a setting in the application.
 *
 * This class encapsulates all the necessary information to define a setting,
 * including its unique key, default value, type, user-facing display name,
 * optional description, and category.
 *
 * @param T The type of the setting's value.
 * @property key A unique string identifier for the setting. This is used to store and retrieve the setting's value.
 * @property defaultValue The default value of the setting if no value has been explicitly set by the user.
 * @property type An instance of [SettingType] that defines how the setting's value is serialized and deserialized.
 * @property displayName A user-friendly name for the setting, displayed in the UI.
 * @property description An optional detailed description of what the setting does, displayed in the UI. Defaults to `null`.
 * @property category A string used to group related settings in the UI. Defaults to "general".
 */
@Immutable
data class SettingDefinition<T>(
    val key: String,
    val defaultValue: T,
    val type: SettingType<T>,
    val displayName: StringResource,
    val description: StringResource? = null,
    val category: StringResource = stringLiteral("general"),
)
