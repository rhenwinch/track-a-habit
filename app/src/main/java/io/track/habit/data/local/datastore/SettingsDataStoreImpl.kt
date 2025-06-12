package io.track.habit.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.local.datastore.entities.UserAppState
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingEntity
import io.track.habit.domain.datastore.SettingType
import io.track.habit.domain.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tah-settings.json")

class SettingsDataStoreImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : SettingsDataStore {
        override val settingsFlow: Flow<AppSettings> =
            context.dataStore.data.map { preferences ->
                AppSettings(
                    general = GeneralSettings.fromPreferences(preferences),
                    appState = UserAppState.fromPreferences(preferences),
                )
            }

        override val appState: Flow<UserAppState> =
            settingsFlow
                .map { it.appState }
                .distinctUntilChanged()

        override val generalSettingsFlow: Flow<GeneralSettings> =
            settingsFlow
                .map { it.general }
                .distinctUntilChanged()

        /**
         * Updates a setting in the DataStore.
         *
         * This function is a generic way to update any setting defined by a [SettingDefinition].
         * It takes the setting definition and the new value as parameters.
         * The function determines the type of the setting from the [SettingDefinition]
         * and updates the corresponding preference in the DataStore.
         *
         * @param T The type of the setting's value.
         * @param definition The [SettingDefinition] of the setting to update.
         * @param value The new value for the setting.
         */
        override suspend fun <T> updateSetting(
            definition: SettingDefinition<T>,
            value: T,
        ) {
            context.dataStore.edit { preferences ->
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

        /**
         * Updates settings in a batch using a generic SettingEntity.
         *
         * This function takes any object that implements the `SettingEntity` interface
         * (like [GeneralSettings] or [UserAppState]) and updates all the corresponding
         * preferences in the DataStore. It utilizes the `toPreferencesMap()` method from the
         * `SettingEntity` to convert the settings object into a map of preference keys and values.
         * This allows for a flexible way to update different groups of settings.
         *
         * @param entity The [SettingEntity] object containing the new values to be applied.
         */
        override suspend fun updateSettings(entity: SettingEntity) {
            context.dataStore.edit { preferences ->
                entity.toPreferencesMap().forEach { (key, value) ->
                    @Suppress("UNCHECKED_CAST")
                    when (value) {
                        is String -> preferences[key as Preferences.Key<String>] = value
                        is Boolean -> preferences[key as Preferences.Key<Boolean>] = value
                        is Int -> preferences[key as Preferences.Key<Int>] = value
                    }
                }
            }
        }

        override suspend fun resetAllSettings() {
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }
