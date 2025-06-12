package io.track.habit.data.local.datastore.entities

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.track.habit.R
import io.track.habit.data.local.datastore.entities.GeneralSettings.Companion.fromPreferences
import io.track.habit.data.local.datastore.entities.GeneralSettingsRegistry.getAllSettings
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingEntity
import io.track.habit.domain.datastore.SettingType
import io.track.habit.domain.utils.stringRes

/**
 * Data class representing the general application settings.
 *
 * This class holds various settings that can be configured by the user,
 * such as their username, whether to censor habit names, if the reset progress
 * button should be locked, and if notifications are enabled.
 *
 * The default values for these settings are sourced from [GeneralSettingsRegistry].
 *
 * The settings can be persisted to and loaded from [Preferences] using the
 * [fromPreferences] and [toPreferencesMap] methods respectively.
 *
 * @property userName The name of the user. Defaults to [GeneralSettingsRegistry.USER_NAME.defaultValue].
 * @property censorHabitNames A boolean indicating whether habit names should be censored. Defaults to [GeneralSettingsRegistry.CENSOR_HABIT_NAMES.defaultValue].
 * @property lockResetProgressButton A boolean indicating whether the reset progress button should be locked. Defaults to [GeneralSettingsRegistry.LOCK_RESET_PROGRESS.defaultValue].
 * @property notificationsEnabled A boolean indicating whether notifications are enabled. Defaults to [GeneralSettingsRegistry.NOTIFICATIONS_ENABLED.defaultValue].
 */
data class GeneralSettings(
    val userName: String = GeneralSettingsRegistry.USER_NAME.defaultValue,
    val censorHabitNames: Boolean = GeneralSettingsRegistry.CENSOR_HABIT_NAMES.defaultValue,
    val lockResetProgressButton: Boolean = GeneralSettingsRegistry.LOCK_RESET_PROGRESS.defaultValue,
    val notificationsEnabled: Boolean = GeneralSettingsRegistry.NOTIFICATIONS_ENABLED.defaultValue,
) : SettingEntity {
    companion object {
        fun fromPreferences(preferences: Preferences): GeneralSettings {
            return GeneralSettings(
                userName =
                    preferences[stringPreferencesKey(GeneralSettingsRegistry.USER_NAME.key)]
                        ?: GeneralSettingsRegistry.USER_NAME.defaultValue,
                censorHabitNames =
                    preferences[booleanPreferencesKey(GeneralSettingsRegistry.CENSOR_HABIT_NAMES.key)]
                        ?: GeneralSettingsRegistry.CENSOR_HABIT_NAMES.defaultValue,
                lockResetProgressButton =
                    preferences[booleanPreferencesKey(GeneralSettingsRegistry.LOCK_RESET_PROGRESS.key)]
                        ?: GeneralSettingsRegistry.LOCK_RESET_PROGRESS.defaultValue,
                notificationsEnabled =
                    preferences[booleanPreferencesKey(GeneralSettingsRegistry.NOTIFICATIONS_ENABLED.key)]
                        ?: GeneralSettingsRegistry.NOTIFICATIONS_ENABLED.defaultValue,
            )
        }
    }

    override fun toPreferencesMap(): Map<Preferences.Key<*>, Any> {
        return mapOf(
            Pair(stringPreferencesKey(GeneralSettingsRegistry.USER_NAME.key), userName),
            Pair(booleanPreferencesKey(GeneralSettingsRegistry.CENSOR_HABIT_NAMES.key), censorHabitNames),
            Pair(booleanPreferencesKey(GeneralSettingsRegistry.LOCK_RESET_PROGRESS.key), lockResetProgressButton),
            Pair(booleanPreferencesKey(GeneralSettingsRegistry.NOTIFICATIONS_ENABLED.key), notificationsEnabled),
        )
    }
}

/**
 * A registry for all general application settings.
 *
 * This object centralizes the definition of all general settings used throughout the application.
 * Each setting is defined as a [SettingDefinition] instance, specifying its key,
 * default value, type, display name, and description.
 *
 * This approach makes it easy to:
 * - Add new settings by simply defining a new [SettingDefinition] property.
 * - Retrieve all settings for UI generation or other purposes using [getAllSettings].
 */
object GeneralSettingsRegistry {
    val USER_NAME =
        SettingDefinition(
            key = "user_name",
            defaultValue = "",
            type = SettingType.StringType,
            displayName = stringRes(R.string.settings_username),
            description = stringRes(R.string.settings_username_desc),
        )

    val CENSOR_HABIT_NAMES =
        SettingDefinition(
            key = "censor_habit_names",
            defaultValue = false,
            type = SettingType.BooleanType,
            displayName = stringRes(R.string.settings_censor_habit_names),
            description = stringRes(R.string.settings_censor_habit_names_desc),
        )

    val LOCK_RESET_PROGRESS =
        SettingDefinition(
            key = "lock_reset_progress_button",
            defaultValue = false,
            type = SettingType.BooleanType,
            displayName = stringRes(R.string.settings_lock_reset_progress),
            description = stringRes(R.string.settings_lock_reset_progress_desc),
        )

    val NOTIFICATIONS_ENABLED =
        SettingDefinition(
            key = "notifications_enabled",
            defaultValue = true,
            type = SettingType.BooleanType,
            displayName = stringRes(R.string.settings_notifications),
            description = stringRes(R.string.settings_notifications_desc),
        )

    fun getAllSettings(): List<SettingDefinition<*>> =
        listOf(
            USER_NAME,
            CENSOR_HABIT_NAMES,
            LOCK_RESET_PROGRESS,
            NOTIFICATIONS_ENABLED,
        )
}
