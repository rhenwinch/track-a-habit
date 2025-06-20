package io.track.habit.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.R
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.remote.drive.AuthorizationState
import io.track.habit.data.remote.drive.GoogleDriveService
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.backup.BackupManager
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingEntity
import io.track.habit.domain.datastore.SettingType
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.model.BackupFile
import io.track.habit.domain.utils.StringResource
import io.track.habit.domain.utils.asStateFlow
import io.track.habit.domain.utils.stringRes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
        private val backupManager: BackupManager,
        private val googleDriveService: GoogleDriveService,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val scope = CoroutineScope(ioDispatcher)
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        private var updateSettingsEntityJob: Job? = null
        private var updateSettingsDefinitionJob: Job? = null

        // Backup operation state
        private val _backupOperationState = MutableStateFlow<BackupOperationState>(BackupOperationState.Idle)
        val backupOperationState: StateFlow<BackupOperationState> = _backupOperationState.asStateFlow()

        // Last backup date
        private val _lastBackupDate = MutableStateFlow<String?>(null)
        val lastBackupDate: StateFlow<String?> = _lastBackupDate.asStateFlow()

        // Available backups
        private val _availableBackups = MutableStateFlow<List<BackupFile>>(emptyList())
        val availableBackups: StateFlow<List<BackupFile>> = _availableBackups.asStateFlow()

        // Direct access to authorization state from GoogleDriveService
        val authorizationState: StateFlow<AuthorizationState> = googleDriveService.authorizationState

        // Derived isSignedIn state from authorizationState
        val isSignedIn = authorizationState
            .map { state ->
                state == AuthorizationState.Authorized
            }.asStateFlow(viewModelScope, initialValue = false)

        // General settings
        val general =
            settingsDataStore
                .generalSettingsFlow
                .asStateFlow(
                    scope = viewModelScope,
                    initialValue = GeneralSettings(),
                )

        init {
            viewModelScope.launch {
                googleDriveService.signIn()
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
            scope.launch {
                _backupOperationState.value = BackupOperationState.SigningIn

                try {
                    googleDriveService.signIn()
                    _backupOperationState.value = BackupOperationState.Idle
                    fetchAvailableBackups()
                } catch (error: Exception) {
                    _backupOperationState.value =
                        BackupOperationState.Error(
                            stringRes(R.string.error_auth_failed_with_reason, error.message ?: ""),
                        )
                }
            }
        }

        /**
         * Signs out from Google Drive.
         */
        fun signOutFromGoogleDrive() {
            scope.launch {
                googleDriveService.signOut()
                _availableBackups.value = emptyList()
                _lastBackupDate.value = null
            }
        }

        /**
         * Creates a backup of the database to Google Drive.
         */
        fun createBackup() {
            scope.launch {
                if (authorizationState.value != AuthorizationState.Authorized) {
                    _backupOperationState.value =
                        BackupOperationState.Error(stringRes(R.string.error_not_signed_in))
                    return@launch
                }

                _backupOperationState.value = BackupOperationState.BackingUp
                backupManager
                    .createBackup()
                    .onSuccess {
                        val timestamp = dateFormat.format(Date())
                        _lastBackupDate.value = timestamp
                        _backupOperationState.value =
                            BackupOperationState.Success(stringRes(R.string.success_backup_created))
                        fetchAvailableBackups()
                    }.onFailure { error ->
                        _backupOperationState.value =
                            BackupOperationState.Error(stringRes(R.string.error_backup_general, error.message ?: ""))
                    }
            }
        }

        /**
         * Restores the database from a selected backup.
         *
         * @param backupId The ID of the backup to restore from
         */
        fun restoreFromBackup(backupId: String) {
            scope.launch {
                if (authorizationState.value != AuthorizationState.Authorized) {
                    _backupOperationState.value =
                        BackupOperationState.Error(stringRes(R.string.error_not_signed_in))
                    return@launch
                }

                _backupOperationState.value = BackupOperationState.Restoring
                backupManager
                    .restoreFromBackup(backupId)
                    .onSuccess {
                        _backupOperationState.value =
                            BackupOperationState.Success(stringRes(R.string.success_restore_completed))
                    }.onFailure { error ->
                        _backupOperationState.value =
                            BackupOperationState.Error(stringRes(R.string.error_restore_general, error.message ?: ""))
                    }
            }
        }

        /**
         * Deletes a specific backup file.
         *
         * @param backupId The ID of the backup to delete
         */
        fun deleteBackup(backupId: String) {
            scope.launch {
                if (authorizationState.value != AuthorizationState.Authorized) {
                    _backupOperationState.value =
                        BackupOperationState.Error(stringRes(R.string.error_not_signed_in))
                    return@launch
                }

                _backupOperationState.value = BackupOperationState.Deleting
                backupManager
                    .deleteBackup(backupId)
                    .onSuccess {
                        _backupOperationState.value =
                            BackupOperationState.Success(stringRes(R.string.success_backup_deleted))
                        fetchAvailableBackups()
                    }.onFailure { error ->
                        _backupOperationState.value =
                            BackupOperationState.Error(stringRes(R.string.error_delete_general, error.message ?: ""))
                    }
            }
        }

        /**
         * Fetches the list of available backups from Google Drive.
         */
        private suspend fun fetchAvailableBackups() {
            // Only proceed if we have Google Drive authorization
            if (authorizationState.value != AuthorizationState.Authorized) {
                return
            }

            backupManager
                .listAvailableBackups()
                .onSuccess { files ->
                    _availableBackups.value = files
                }.onFailure {
                    _availableBackups.value = emptyList()
                }
        }
    }

/**
 * Represents the state of backup operations.
 */
sealed class BackupOperationState {
    data object Idle : BackupOperationState()

    data object SigningIn : BackupOperationState()

    data object BackingUp : BackupOperationState()

    data object Restoring : BackupOperationState()

    data object Deleting : BackupOperationState()

    data class Success(
        val message: StringResource,
    ) : BackupOperationState()

    data class Error(
        val message: StringResource,
    ) : BackupOperationState()
}
