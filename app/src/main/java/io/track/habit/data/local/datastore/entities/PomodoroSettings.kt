package io.track.habit.data.local.datastore.entities

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import io.track.habit.R
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingEntity
import io.track.habit.domain.datastore.SettingType
import io.track.habit.domain.utils.stringRes

/**
 * Data class representing Pomodoro settings.
 *
 * @property restTimeInMs The duration of the rest period in milliseconds.
 *                       Defaults to the value defined in [PomodoroSettingsRegistry.REST_TIME_IN_MS].
 * @property workTimeInMs The duration of the work period in milliseconds.
 *                       Defaults to the value defined in [PomodoroSettingsRegistry.WORK_TIME_IN_MS].
 */
data class PomodoroSettings(
    val restTimeInMs: Long = PomodoroSettingsRegistry.REST_TIME_IN_MS.defaultValue,
    val workTimeInMs: Long = PomodoroSettingsRegistry.WORK_TIME_IN_MS.defaultValue,
) : SettingEntity {
    companion object {
        fun fromPreferences(preferences: Preferences): PomodoroSettings {
            return PomodoroSettings(
                restTimeInMs =
                    preferences[longPreferencesKey(PomodoroSettingsRegistry.REST_TIME_IN_MS.key)]
                        ?: PomodoroSettingsRegistry.REST_TIME_IN_MS.defaultValue,
                workTimeInMs =
                    preferences[longPreferencesKey(PomodoroSettingsRegistry.WORK_TIME_IN_MS.key)]
                        ?: PomodoroSettingsRegistry.WORK_TIME_IN_MS.defaultValue,
            )
        }
    }

    override fun toPreferencesMap(): Map<Preferences.Key<*>, Any> {
        return mapOf(
            Pair(longPreferencesKey(PomodoroSettingsRegistry.REST_TIME_IN_MS.key), restTimeInMs),
            Pair(longPreferencesKey(PomodoroSettingsRegistry.WORK_TIME_IN_MS.key), workTimeInMs),
        )
    }
}

/**
 * Defines the settings related to the Pomodoro timer.
 *
 * This object holds the definitions for various Pomodoro settings, such as rest time and work time.
 * Each setting is defined as a [SettingDefinition] object, which includes a key, default value,
 * type, display name, and description. These definitions are used to manage and access Pomodoro
 * settings within the application.
 */
object PomodoroSettingsRegistry {
    val REST_TIME_IN_MS =
        SettingDefinition(
            key = "rest_time_in_ms",
            defaultValue = 5 * (1000 * 60),
            type = SettingType.LongType,
            displayName = stringRes(R.string.settings_rest_time),
            description = stringRes(R.string.settings_rest_time_desc),
        )

    val WORK_TIME_IN_MS =
        SettingDefinition(
            key = "work_time_in_ms",
            defaultValue = 25 * (1000 * 60),
            type = SettingType.LongType,
            displayName = stringRes(R.string.settings_work_time),
            description = stringRes(R.string.settings_work_time_desc),
        )
}
