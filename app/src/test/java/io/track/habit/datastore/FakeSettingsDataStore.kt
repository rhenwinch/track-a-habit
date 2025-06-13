package io.track.habit.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.track.habit.data.local.datastore.AppSettings
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.local.datastore.entities.UserAppState
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingEntity
import io.track.habit.domain.datastore.SettingType
import io.track.habit.domain.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FakeSettingsDataStore(
    private val testDataStore: DataStore<Preferences>,
) : SettingsDataStore {
    override val settingsFlow: Flow<AppSettings> =
        testDataStore.data.map { preferences ->
            AppSettings(
                general = GeneralSettings.fromPreferences(preferences),
                appState = UserAppState.fromPreferences(preferences),
            )
        }

    override val appStateFlow: Flow<UserAppState> =
        settingsFlow
            .map { it.appState }
            .distinctUntilChanged()

    override val generalSettingsFlow: Flow<GeneralSettings> =
        settingsFlow
            .map { it.general }
            .distinctUntilChanged()

    override suspend fun <T> updateSetting(
        definition: SettingDefinition<T>,
        value: T,
    ) {
        testDataStore.edit { preferences ->
            when (definition.type) {
                is SettingType.StringType -> {
                    preferences[stringPreferencesKey(definition.key)] = value as String
                }

                is SettingType.BooleanType -> {
                    preferences[booleanPreferencesKey(definition.key)] = value as Boolean
                }

                is SettingType.IntType -> {
                    preferences[intPreferencesKey(definition.key)] = value as Int
                }

                is SettingType.LongType -> {
                    preferences[longPreferencesKey(definition.key)] = value as Long
                }
            }
        }
    }

    override suspend fun updateSettings(entity: SettingEntity) {
        testDataStore.edit { preferences ->
            entity.toPreferencesMap().forEach { (key, value) ->
                @Suppress("UNCHECKED_CAST")
                when (value) {
                    is String -> preferences[key as Preferences.Key<String>] = value
                    is Boolean -> preferences[key as Preferences.Key<Boolean>] = value
                    is Int -> preferences[key as Preferences.Key<Int>] = value
                    is Long -> preferences[key as Preferences.Key<Long>] = value
                }
            }
        }
    }

    override suspend fun resetAllSettings() {
        testDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
