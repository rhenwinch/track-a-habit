package io.track.habit.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingEntity
import io.track.habit.domain.datastore.SettingType
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.utils.asStateFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsDataStore: SettingsDataStore,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val scope = CoroutineScope(ioDispatcher)

        private var updateSettingsEntityJob: Job? = null
        private var updateSettingsDefinitionJob: Job? = null

        val general =
            settingsDataStore
                .generalSettingsFlow
                .asStateFlow(
                    scope = viewModelScope,
                    initialValue = GeneralSettings(),
                )

        fun updateSettings(setting: SettingEntity) {
            if (updateSettingsEntityJob?.isActive == true) return

            updateSettingsEntityJob =
                scope.launch {
                    settingsDataStore.updateSettings(setting)
                }
        }

        /**
         * Updates a setting with proper type casting from the UI layer.
         * This function safely handles the wildcard type projection by checking
         * the setting type and casting the value appropriately.
         *
         * @param definition The setting definition with wildcard type
         * @param value The value to update the setting with
         */
        fun updateSettingWithCast(
            definition: SettingDefinition<*>,
            value: Any,
        ) {
            if (updateSettingsDefinitionJob?.isActive == true) return

            updateSettingsDefinitionJob =
                scope.launch {
                    @Suppress("UNCHECKED_CAST")
                    when (definition.type) {
                        is SettingType.StringType -> {
                            settingsDataStore.updateSetting(
                                definition as SettingDefinition<String>,
                                value as String,
                            )
                        }
                        is SettingType.BooleanType -> {
                            settingsDataStore.updateSetting(
                                definition as SettingDefinition<Boolean>,
                                value as Boolean,
                            )
                        }
                        is SettingType.IntType -> {
                            settingsDataStore.updateSetting(
                                definition as SettingDefinition<Int>,
                                value as Int,
                            )
                        }
                        is SettingType.LongType -> {
                            settingsDataStore.updateSetting(
                                definition as SettingDefinition<Long>,
                                value as Long,
                            )
                        }
                    }
                }
        }
    }
