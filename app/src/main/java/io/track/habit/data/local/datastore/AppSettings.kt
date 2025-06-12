package io.track.habit.data.local.datastore

import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.local.datastore.entities.UserAppState

/**
 * Represents the application settings.
 *
 * @property general The general settings for the application.
 */
data class AppSettings(
    val general: GeneralSettings,
    val appState: UserAppState,
    // ... Add other settings as needed, e.g., notifications, themes, etc.
)
