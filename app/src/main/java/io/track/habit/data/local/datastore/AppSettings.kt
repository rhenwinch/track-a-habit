package io.track.habit.data.local.datastore

import io.track.habit.data.local.datastore.entities.GeneralSettings

/**
 * Represents the application settings.
 *
 * @property general The general settings for the application.
 */
data class AppSettings(
    val general: GeneralSettings,
    // ... Add other settings as needed, e.g., notifications, themes, etc.
)
