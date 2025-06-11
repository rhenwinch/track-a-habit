package io.track.habit.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingEntity
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.utils.asStateFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel
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

        fun <T> updateSettingsDefinition(
            setting: SettingDefinition<T>,
            value: T,
        ) {
            if (updateSettingsDefinitionJob?.isActive == true) return

            updateSettingsDefinitionJob =
                scope.launch {
                    settingsDataStore.updateSetting(setting, value)
                }
        }
    }
