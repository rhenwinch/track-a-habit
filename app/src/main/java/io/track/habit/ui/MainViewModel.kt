package io.track.habit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.utils.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        settingsDataStore: SettingsDataStore,
    ) : ViewModel() {
        val isFirstRun =
            settingsDataStore.appStateFlow
                .map { it.isFirstRun }
                .distinctUntilChanged()
                .asStateFlow(scope = viewModelScope)
    }
