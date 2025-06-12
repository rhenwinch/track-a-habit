package io.track.habit.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.track.habit.data.local.datastore.AppSettings
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.local.datastore.entities.GeneralSettingsRegistry
import io.track.habit.datastore.FakeSettingsDataStore
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingsDataStore
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
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

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

    private val generalSettings =
        GeneralSettings(
            userName = "Test User",
            lastShowcasedHabitId = 123L,
            censorHabitNames = true,
            lockResetProgressButton = false,
            notificationsEnabled = true,
        )

    private val generalSettingsFlow = MutableStateFlow(generalSettings)
    private val appSettingsFlow = MutableStateFlow(AppSettings(general = generalSettings))

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

        viewModel =
            SettingsViewModel(
                settingsDataStore = settingsDataStore,
                ioDispatcher = testDispatcher,
            )

        mockViewModel =
            SettingsViewModel(
                settingsDataStore = mockSettingsDataStore,
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
                    lastShowcasedHabitId = 456L,
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
                    get { lastShowcasedHabitId }.isEqualTo(456L)
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
}
