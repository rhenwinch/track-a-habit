package io.track.habit.data.local.datastore

import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.local.datastore.entities.PomodoroSettings

/**
 * Represents the application settings.
 *
 * @property general The general settings for the application.
 * @property pomodoro The Pomodoro timer settings for the application.
 */
data class AppSettings(
    val general: GeneralSettings,
    val pomodoro: PomodoroSettings,
)
