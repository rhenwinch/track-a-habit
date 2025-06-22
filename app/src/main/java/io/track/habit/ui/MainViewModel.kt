package io.track.habit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.data.remote.drive.GoogleDriveService
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.utils.asStateFlow
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        settingsDataStore: SettingsDataStore,
        val googleDriveService: GoogleDriveService,
    ) : ViewModel() {
        val isFirstRun =
            settingsDataStore.appStateFlow
                .map { it.isFirstRun }
                .distinctUntilChanged()
                .asStateFlow(scope = viewModelScope)

        init {
            viewModelScope.launch {
                combine(
                    googleDriveService.isInitialized,
                    isFirstRun,
                ) { initialized, firstRun ->
                    if (initialized && !firstRun) {
                        googleDriveService.signIn()
                        cancel() // Cancel the coroutine after signing in to avoid unnecessary emissions
                    }
                }.collect()
            }
        }
    }
