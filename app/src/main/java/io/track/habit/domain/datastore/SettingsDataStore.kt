package io.track.habit.domain.datastore

import io.track.habit.data.local.datastore.AppSettings
import io.track.habit.data.local.datastore.entities.GeneralSettings
import kotlinx.coroutines.flow.Flow

interface SettingsDataStore {
    val settingsFlow: Flow<AppSettings>
    val generalSettingsFlow: Flow<GeneralSettings>

    suspend fun <T> updateSetting(
        definition: SettingDefinition<T>,
        value: T,
    )

    suspend fun updateSettings(entity: SettingEntity)

    suspend fun resetAllSettings()
}
