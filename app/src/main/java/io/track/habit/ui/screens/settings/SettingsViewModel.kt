package io.track.habit.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.remote.backup.RemoteBackupManager
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingEntity
import io.track.habit.domain.datastore.SettingType
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.utils.asStateFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for the Settings screen that handles user preferences and backup operations.
 */
@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsDataStore: SettingsDataStore,
        private val remoteBackupManager: RemoteBackupManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val scope = CoroutineScope(ioDispatcher)
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        private var updateSettingsEntityJob: Job? = null
        private var updateSettingsDefinitionJob: Job? = null

        // Google Drive backup state
        private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
        val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

        private val _isSignedIn = MutableStateFlow(false)
        val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

        private val _lastBackupDate = MutableStateFlow<String?>(null)
        val lastBackupDate: StateFlow<String?> = _lastBackupDate.asStateFlow()

        private val _availableBackups = MutableStateFlow<List<BackupFile>>(emptyList())
        val availableBackups: StateFlow<List<BackupFile>> = _availableBackups.asStateFlow()

        val general =
            settingsDataStore
                .generalSettingsFlow
                .asStateFlow(
                    scope = viewModelScope,
                    initialValue = GeneralSettings(),
                )

        init {
            viewModelScope.launch {
                checkSignInStatus()
                fetchAvailableBackups()
            }
        }

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

        /**
         * Initiates sign-in to Google Drive.
         */
        fun signInToGoogleDrive() {
            viewModelScope.launch {
                _backupState.value = BackupState.SigningIn
                remoteBackupManager
                    .signIn()
                    .onSuccess {
                        _isSignedIn.value = true
                        _backupState.value = BackupState.Idle
                        fetchAvailableBackups()
                    }.onFailure { error ->
                        _backupState.value = BackupState.Error(error.message ?: "Sign-in failed")
                    }
            }
        }

        /**
         * Signs out from Google Drive.
         */
        fun signOutFromGoogleDrive() {
            viewModelScope.launch {
                remoteBackupManager.signOut()
                _isSignedIn.value = false
                _availableBackups.value = emptyList()
                _lastBackupDate.value = null
            }
        }

        /**
         * Creates a backup of the database to Google Drive.
         */
        fun createBackup() {
            viewModelScope.launch {
                if (!remoteBackupManager.hasGoogleDrivePermission()) {
                    _backupState.value = BackupState.Error("Not signed in to Google Drive")
                    return@launch
                }

                _backupState.value = BackupState.BackingUp
                remoteBackupManager
                    .createBackup()
                    .onSuccess { file ->
                        val timestamp = dateFormat.format(Date())
                        _lastBackupDate.value = timestamp
                        _backupState.value = BackupState.Success("Backup created successfully")
                        fetchAvailableBackups()
                    }.onFailure { error ->
                        _backupState.value = BackupState.Error(error.message ?: "Backup failed")
                    }
            }
        }

        /**
         * Restores the database from a selected backup.
         *
         * @param backupId The ID of the backup to restore from
         */
        fun restoreFromBackup(backupId: String) {
            viewModelScope.launch {
                if (!remoteBackupManager.hasGoogleDrivePermission()) {
                    _backupState.value = BackupState.Error("Not signed in to Google Drive")
                    return@launch
                }

                _backupState.value = BackupState.Restoring
                remoteBackupManager
                    .restoreFromBackup(backupId)
                    .onSuccess {
                        _backupState.value = BackupState.Success("Restore completed successfully")
                    }.onFailure { error ->
                        _backupState.value = BackupState.Error(error.message ?: "Restore failed")
                    }
            }
        }

        /**
         * Deletes a specific backup file.
         *
         * @param backupId The ID of the backup to delete
         */
        fun deleteBackup(backupId: String) {
            viewModelScope.launch {
                if (!remoteBackupManager.hasGoogleDrivePermission()) {
                    _backupState.value = BackupState.Error("Not signed in to Google Drive")
                    return@launch
                }

                _backupState.value = BackupState.Deleting
                remoteBackupManager
                    .deleteBackup(backupId)
                    .onSuccess {
                        _backupState.value = BackupState.Success("Backup deleted successfully")
                        fetchAvailableBackups()
                    }.onFailure { error ->
                        _backupState.value = BackupState.Error(error.message ?: "Delete failed")
                    }
            }
        }

        /**
         * Checks if the user is signed in to Google Drive.
         */
        private suspend fun checkSignInStatus() {
            _isSignedIn.value = remoteBackupManager.hasGoogleDrivePermission()
        }

        /**
         * Fetches the list of available backups from Google Drive.
         */
        private suspend fun fetchAvailableBackups() {
            if (!remoteBackupManager.hasGoogleDrivePermission()) {
                return
            }

            remoteBackupManager
                .listAvailableBackups()
                .onSuccess { files ->
                    _availableBackups.value = files.mapNotNull { file ->
                        val parts = file.absolutePath.split("::")
                        if (parts.size == 2) {
                            val id = parts[0]
                            val name = File(parts[1]).name
                            BackupFile(id, name)
                        } else {
                            null
                        }
                    }
                }.onFailure {
                    _availableBackups.value = emptyList()
                }
        }
    }

/**
 * Represents the state of backup operations.
 */
sealed class BackupState {
    object Idle : BackupState()

    object SigningIn : BackupState()

    object BackingUp : BackupState()

    object Restoring : BackupState()

    object Deleting : BackupState()

    data class Success(
        val message: String,
    ) : BackupState()

    data class Error(
        val message: String,
    ) : BackupState()
}

/**
 * Represents a backup file with its ID and name.
 */
data class BackupFile(
    val id: String,
    val name: String,
)
