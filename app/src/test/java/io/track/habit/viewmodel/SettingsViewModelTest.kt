package io.track.habit.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.track.habit.data.local.datastore.AppSettings
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.local.datastore.entities.GeneralSettingsRegistry
import io.track.habit.data.local.datastore.entities.UserAppState
import io.track.habit.data.remote.drive.AuthorizationState
import io.track.habit.data.remote.drive.GoogleDriveService
import io.track.habit.datastore.FakeSettingsDataStore
import io.track.habit.domain.backup.BackupManager
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.model.BackupFile
import io.track.habit.ui.screens.settings.BackupOperationState
import io.track.habit.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var mockSettingsDataStore: SettingsDataStore

    private lateinit var viewModel: SettingsViewModel
    private lateinit var mockViewModel: SettingsViewModel
    private lateinit var mockBackupManager: BackupManager
    private lateinit var mockGoogleDriveService: GoogleDriveService
    private lateinit var viewModelWithBackup: SettingsViewModel

    private val generalSettings =
        GeneralSettings(
            userName = "Test User",
            censorHabitNames = true,
            lockResetProgressButton = false,
            notificationsEnabled = true,
        )

    private val generalSettingsFlow = MutableStateFlow(generalSettings)
    private val appSettingsFlow =
        MutableStateFlow(
            AppSettings(general = generalSettings, appState = UserAppState()),
        )

    private val authStateFlow = MutableStateFlow<AuthorizationState>(AuthorizationState.NotAuthorized)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        testDataStore =
            PreferenceDataStoreFactory.create(
                scope = TestScope(testDispatcher + Job()),
                produceFile = { temporaryFolder.newFile("test_settings.preferences_pb") },
            )

        settingsDataStore = FakeSettingsDataStore(testDataStore)

        mockSettingsDataStore = mockk<SettingsDataStore>(relaxed = true)
        coEvery { mockSettingsDataStore.generalSettingsFlow } returns appSettingsFlow.map { it.general }
        coEvery { mockSettingsDataStore.settingsFlow } returns appSettingsFlow

        // Initialize mock backup services
        mockBackupManager = mockk<BackupManager>(relaxed = true)
        mockGoogleDriveService = mockk<GoogleDriveService>(relaxed = true)
        every { mockGoogleDriveService.authorizationState } returns authStateFlow

        // Initialize ViewModels with proper dependencies
        viewModel =
            SettingsViewModel(
                settingsDataStore = settingsDataStore,
                backupManager = mockBackupManager,
                googleDriveService = mockGoogleDriveService,
                ioDispatcher = testDispatcher,
            )

        mockViewModel =
            SettingsViewModel(
                settingsDataStore = mockSettingsDataStore,
                backupManager = mockBackupManager,
                googleDriveService = mockGoogleDriveService,
                ioDispatcher = testDispatcher,
            )

        viewModelWithBackup =
            SettingsViewModel(
                settingsDataStore = mockSettingsDataStore,
                backupManager = mockBackupManager,
                googleDriveService = mockGoogleDriveService,
                ioDispatcher = testDispatcher,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `general settings flow emits data from datastore`() =
        runTest(testDispatcher) {
            settingsDataStore.updateSetting(GeneralSettingsRegistry.USER_NAME, "Test User")
            settingsDataStore.updateSetting(GeneralSettingsRegistry.CENSOR_HABIT_NAMES, true)
            settingsDataStore.updateSetting(GeneralSettingsRegistry.LOCK_RESET_PROGRESS, false)
            settingsDataStore.updateSetting(GeneralSettingsRegistry.NOTIFICATIONS_ENABLED, true)

            viewModel.general.test {
                skipItems(1) // Skip initial emission
                val emittedSettings = awaitItem()
                expectThat(emittedSettings) {
                    get { userName }.isEqualTo("Test User")
                    get { censorHabitNames }.isTrue()
                    get { lockResetProgressButton }.isFalse()
                    get { notificationsEnabled }.isTrue()
                }
            }
        }

    @Test
    fun `updateSettings calls settings datastore`() =
        runTest(testDispatcher) {
            val updatedSettings = generalSettings.copy(userName = "Updated User")

            mockViewModel.updateSettings(updatedSettings)
            advanceUntilIdle()

            coVerify { mockSettingsDataStore.updateSettings(updatedSettings) }
        }

    @Test
    fun `updateSettingWithCast calls settings datastore with correct parameters`() =
        runTest(testDispatcher) {
            val setting = GeneralSettingsRegistry.USER_NAME
            val value = "New Username"

            mockViewModel.updateSettingWithCast(setting, value)
            advanceUntilIdle()

            coVerify {
                mockSettingsDataStore.updateSetting(
                    withArg { definition ->
                        expectThat(definition).get { key }.isEqualTo(setting.key)
                    },
                    value,
                )
            }
        }

    @Test
    fun `updateSettings persists changes to datastore`() =
        runTest(testDispatcher) {
            val updatedSettings =
                GeneralSettings(
                    userName = "Persisted User",
                    censorHabitNames = false,
                    lockResetProgressButton = true,
                    notificationsEnabled = false,
                )

            viewModel.updateSettings(updatedSettings)
            advanceUntilIdle()

            viewModel.general.test {
                skipItems(1) // Skip initial emission
                val emittedSettings = awaitItem()
                expectThat(emittedSettings) {
                    get { userName }.isEqualTo("Persisted User")
                    get { censorHabitNames }.isFalse()
                    get { lockResetProgressButton }.isTrue()
                    get { notificationsEnabled }.isFalse()
                }
            }
        }

    @Test
    fun `updateSettingWithCast persists changes to datastore`() =
        runTest(testDispatcher) {
            viewModel.updateSettingWithCast(GeneralSettingsRegistry.USER_NAME, "Definition User")
            advanceUntilIdle()

            viewModel.general.test {
                skipItems(1) // Skip initial emission
                val emittedSettings = awaitItem()
                expectThat(emittedSettings).get { userName }.isEqualTo("Definition User")
            }
        }

    @Test
    fun `updateSettings ignores call when previous job is active`() =
        runTest(testDispatcher) {
            val slot = slot<GeneralSettings>()
            var callCount = 0

            coEvery { mockSettingsDataStore.updateSettings(capture(slot)) } answers {
                callCount++
                // Don't complete immediately to simulate a job that's still running
            }

            mockViewModel.updateSettings(generalSettings)
            mockViewModel.updateSettings(generalSettings.copy(userName = "Second Call"))

            advanceUntilIdle()

            expectThat(callCount).isEqualTo(1)
            expectThat(slot.captured).get { userName }.isEqualTo("Test User")
        }

    @Test
    fun `updateSettingWithCast ignores call when previous job is active`() =
        runTest(testDispatcher) {
            val settingSlot = slot<SettingDefinition<String>>()
            val valueSlot = slot<String>()
            var callCount = 0

            coEvery {
                mockSettingsDataStore.updateSetting(
                    capture(settingSlot),
                    capture(valueSlot),
                )
            } answers {
                callCount++
                // Don't complete immediately to simulate a job that's still running
            }

            mockViewModel.updateSettingWithCast(GeneralSettingsRegistry.USER_NAME, "First Call")
            mockViewModel.updateSettingWithCast(GeneralSettingsRegistry.USER_NAME, "Second Call")

            advanceUntilIdle()

            expectThat(callCount).isEqualTo(1)
            expectThat(valueSlot.captured).isEqualTo("First Call")
        }

    @Test
    fun `isSignedIn reflects the authorized state from GoogleDriveService`() =
        runTest(testDispatcher) {
            turbineScope {
                val isSignedInTurbine = viewModelWithBackup.isSignedIn.testIn(this)

                // Initial state should be false
                expectThat(isSignedInTurbine.awaitItem()).isFalse()

                // Update the authorization state to Authorized
                authStateFlow.value = AuthorizationState.Authorized
                expectThat(isSignedInTurbine.awaitItem()).isTrue()

                // Update the authorization state back to NotAuthorized
                authStateFlow.value = AuthorizationState.NotAuthorized
                expectThat(isSignedInTurbine.awaitItem()).isFalse()

                isSignedInTurbine.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `signInToGoogleDrive calls service and updates state`() =
        runTest(testDispatcher) {
            turbineScope {
                val operationStateTurbine = viewModelWithBackup.backupOperationState.testIn(this)

                // Skip initial Idle state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Idle>()

                // Setup success response from Google Drive service
                coEvery { mockGoogleDriveService.signIn() } answers {
                    authStateFlow.value = AuthorizationState.Authorized
                    Result.success(Unit)
                }

                // Call the sign-in method
                viewModelWithBackup.signInToGoogleDrive()

                // Should update to SigningIn state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.SigningIn>()

                // Should return to Idle state after successful sign-in
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Idle>()

                // Verify the GoogleDriveService signIn method was called
                coVerify { mockGoogleDriveService.signIn() }
                coVerify { mockBackupManager.listAvailableBackups() }

                operationStateTurbine.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `signInToGoogleDrive handles errors correctly`() =
        runTest(testDispatcher) {
            turbineScope {
                val operationStateTurbine = viewModelWithBackup.backupOperationState.testIn(this)

                // Skip initial Idle state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Idle>()

                // Setup error response from Google Drive service
                val exception = Exception("Authentication failed")
                coEvery { mockGoogleDriveService.signIn() } answers {
                    throw exception
                }

                // Call the sign-in method
                viewModelWithBackup.signInToGoogleDrive()

                // Should update to SigningIn state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.SigningIn>()

                // Should update to Error state with the error message
                val errorState = operationStateTurbine.awaitItem()
                expectThat(errorState).isA<BackupOperationState.Error>()

                operationStateTurbine.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `signOutFromGoogleDrive calls service and clears backup data`() =
        runTest(testDispatcher) {
            turbineScope {
                val backupsTurbine = viewModelWithBackup.availableBackups.testIn(this)
                val lastBackupDateTurbine = viewModelWithBackup.lastBackupDate.testIn(this)

                // Initial state
                expectThat(backupsTurbine.awaitItem()).isEmpty()
                expectThat(lastBackupDateTurbine.awaitItem()).isEqualTo(null)

                // Call sign out
                viewModelWithBackup.signOutFromGoogleDrive()
                advanceUntilIdle()

                // Verify GoogleDriveService signOut was called
                coVerify { mockGoogleDriveService.signOut() }

                backupsTurbine.cancelAndIgnoreRemainingEvents()
                lastBackupDateTurbine.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `createBackup success updates state properly`() =
        runTest(testDispatcher) {
            turbineScope {
                val operationStateTurbine = viewModelWithBackup.backupOperationState.testIn(this)
                val lastBackupDateTurbine = viewModelWithBackup.lastBackupDate.testIn(this)
                val backupsTurbine = viewModelWithBackup.availableBackups.testIn(this)

                // Skip initial states
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Idle>()
                expectThat(lastBackupDateTurbine.awaitItem()).isEqualTo(null)
                expectThat(backupsTurbine.awaitItem()).isEmpty()

                // Set authorized state
                authStateFlow.value = AuthorizationState.Authorized

                // Setup successful backup creation
                val backupFile = File("backup.zip")
                coEvery { mockBackupManager.createBackup() } returns Result.success(backupFile)

                // Mock available backups after creation
                val mockBackups = listOf(
                    BackupFile("backup1", "Backup 1"),
                    BackupFile("backup2", "Backup 2"),
                )
                coEvery { mockBackupManager.listAvailableBackups() } returns Result.success(mockBackups)

                // Create backup
                viewModelWithBackup.createBackup()

                // Should update to BackingUp state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.BackingUp>()

                // Should update to Success state
                val successState = operationStateTurbine.awaitItem()
                expectThat(successState).isA<BackupOperationState.Success>()

                // Last backup date should be updated
                val lastBackupDate = lastBackupDateTurbine.awaitItem()
                expectThat(lastBackupDate).isNotNull()

                // Available backups should be updated
                val availableBackups = backupsTurbine.awaitItem()
                expectThat(availableBackups).isEqualTo(mockBackups)

                // Verify BackupManager was called
                coVerify { mockBackupManager.createBackup() }
                coVerify { mockBackupManager.listAvailableBackups() }

                operationStateTurbine.cancelAndIgnoreRemainingEvents()
                lastBackupDateTurbine.cancelAndIgnoreRemainingEvents()
                backupsTurbine.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `createBackup fails when not signed in`() =
        runTest(testDispatcher) {
            turbineScope {
                val operationStateTurbine = viewModelWithBackup.backupOperationState.testIn(this)

                // Skip initial state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Idle>()

                // Ensure not authorized
                authStateFlow.value = AuthorizationState.NotAuthorized

                // Create backup
                viewModelWithBackup.createBackup()

                // Should update to Error state
                val errorState = operationStateTurbine.awaitItem()
                expectThat(errorState).isA<BackupOperationState.Error>()

                // BackupManager should not be called
                coVerify(exactly = 0) { mockBackupManager.createBackup() }

                operationStateTurbine.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `createBackup handles errors from BackupManager`() =
        runTest(testDispatcher) {
            turbineScope {
                val operationStateTurbine = viewModelWithBackup.backupOperationState.testIn(this)

                // Skip initial state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Idle>()

                // Set authorized state
                authStateFlow.value = AuthorizationState.Authorized

                // Setup error response
                val exception = Exception("Backup failed")
                coEvery { mockBackupManager.createBackup() } returns Result.failure(exception)

                // Create backup
                viewModelWithBackup.createBackup()

                // Should update to BackingUp state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.BackingUp>()

                // Should update to Error state
                val errorState = operationStateTurbine.awaitItem()
                expectThat(errorState).isA<BackupOperationState.Error>()

                operationStateTurbine.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `restoreFromBackup success updates state properly`() =
        runTest(testDispatcher) {
            turbineScope {
                val operationStateTurbine = viewModelWithBackup.backupOperationState.testIn(this)

                // Skip initial state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Idle>()

                // Set authorized state
                authStateFlow.value = AuthorizationState.Authorized

                // Setup successful restore
                val backupId = "backup1"
                coEvery { mockBackupManager.restoreFromBackup(backupId) } returns Result.success(Unit)

                // Restore backup
                viewModelWithBackup.restoreFromBackup(backupId)

                // Should update to Restoring state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Restoring>()

                // Should update to Success state
                val successState = operationStateTurbine.awaitItem()
                expectThat(successState).isA<BackupOperationState.Success>()

                // Verify BackupManager was called
                coVerify { mockBackupManager.restoreFromBackup(backupId) }

                operationStateTurbine.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `restoreFromBackup fails when not signed in`() =
        runTest(testDispatcher) {
            turbineScope {
                val operationStateTurbine = viewModelWithBackup.backupOperationState.testIn(this)

                // Skip initial state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Idle>()

                // Ensure not authorized
                authStateFlow.value = AuthorizationState.NotAuthorized

                // Restore backup
                viewModelWithBackup.restoreFromBackup("backup1")

                // Should update to Error state
                val errorState = operationStateTurbine.awaitItem()
                expectThat(errorState).isA<BackupOperationState.Error>()

                // BackupManager should not be called
                coVerify(exactly = 0) { mockBackupManager.restoreFromBackup(any()) }

                operationStateTurbine.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `deleteBackup success updates state properly`() =
        runTest(testDispatcher) {
            turbineScope {
                val operationStateTurbine = viewModelWithBackup.backupOperationState.testIn(this)
                val backupsTurbine = viewModelWithBackup.availableBackups.testIn(this)

                // Skip initial states
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Idle>()
                expectThat(backupsTurbine.awaitItem()).isEmpty()

                // Set authorized state
                authStateFlow.value = AuthorizationState.Authorized

                // Setup successful delete
                val backupId = "backup1"
                coEvery { mockBackupManager.deleteBackup(backupId) } returns Result.success(Unit)

                // Mock updated backups list after deletion
                val mockBackupsAfterDelete = listOf(
                    BackupFile("backup2", "Backup 2"),
                )
                coEvery { mockBackupManager.listAvailableBackups() } returns Result.success(mockBackupsAfterDelete)

                // Delete backup
                viewModelWithBackup.deleteBackup(backupId)

                // Should update to Deleting state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Deleting>()

                // Should update to Success state
                val successState = operationStateTurbine.awaitItem()
                expectThat(successState).isA<BackupOperationState.Success>()

                // Available backups should be updated
                val availableBackups = backupsTurbine.awaitItem()
                expectThat(availableBackups).isEqualTo(mockBackupsAfterDelete)

                // Verify BackupManager was called
                coVerify { mockBackupManager.deleteBackup(backupId) }
                coVerify { mockBackupManager.listAvailableBackups() }

                operationStateTurbine.cancelAndIgnoreRemainingEvents()
                backupsTurbine.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `resetBackupOperationState sets state to idle`() =
        runTest(testDispatcher) {
            turbineScope {
                val operationStateTurbine = viewModelWithBackup.backupOperationState.testIn(this)

                // Skip initial state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Idle>()

                // Set a non-idle state first
                authStateFlow.value = AuthorizationState.Authorized

                // Setup error for operation
                coEvery { mockBackupManager.createBackup() } returns Result.failure(Exception("Test error"))

                // Create backup to trigger error state
                viewModelWithBackup.createBackup()

                // Should update to BackingUp state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.BackingUp>()

                // Should update to Error state
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Error>()

                // Reset state
                viewModelWithBackup.resetBackupOperationState()

                // Should return to Idle
                expectThat(operationStateTurbine.awaitItem()).isA<BackupOperationState.Idle>()

                operationStateTurbine.cancelAndIgnoreRemainingEvents()
            }
        }
}
