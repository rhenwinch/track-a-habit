package io.track.habit.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.local.datastore.entities.GeneralSettingsRegistry
import io.track.habit.data.local.datastore.entities.PomodoroSettings
import io.track.habit.data.local.datastore.entities.PomodoroSettingsRegistry
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.datastore.SettingType
import io.track.habit.domain.datastore.SettingsDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@ExperimentalCoroutinesApi
class SettingsDataStoreTest {
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var settingsDataStore: SettingsDataStore

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    @get:Rule
    val temporaryFolder: TemporaryFolder =
        TemporaryFolder
            .builder()
            .assureDeletion()
            .build()

    @Before
    fun setup() {
        testDataStore =
            PreferenceDataStoreFactory.create(
                scope = testScope,
                produceFile = { temporaryFolder.newFile("test_tah_settings.preferences_pb") },
            )

        settingsDataStore = FakeSettingsDataStore(testDataStore)
    }

    @Test
    fun `initial settings should have default values`() =
        runTest {
            val settings = settingsDataStore.settingsFlow.first()

            assertEquals("", settings.general.userName)
            assertFalse(settings.general.censorHabitNames)
            assertFalse(settings.general.lockResetProgressButton)

            assertEquals(5 * (1000 * 60), settings.pomodoro.restTimeInMs)
            assertEquals(25 * (1000 * 60), settings.pomodoro.workTimeInMs)
        }

    @Test
    fun `generalSettingsFlow should emit only general settings`() =
        runTest {
            val initialGeneral = settingsDataStore.generalSettingsFlow.first()
            assertEquals("", initialGeneral.userName)
            assertFalse(initialGeneral.censorHabitNames)
            assertFalse(initialGeneral.lockResetProgressButton)
        }

    @Test
    fun `pomodoroSettingsFlow should emit only pomodoro settings`() =
        runTest {
            val initialPomodoro = settingsDataStore.pomodoroSettingsFlow.first()
            assertEquals(5 * (1000 * 60), initialPomodoro.restTimeInMs)
            assertEquals(25 * (1000 * 60), initialPomodoro.workTimeInMs)
        }

    @Test
    fun `updateSetting should work for string settings`() =
        runTest {
            val testName = "John Doe"

            settingsDataStore.updateSetting(GeneralSettingsRegistry.USER_NAME, testName)

            val settings = settingsDataStore.settingsFlow.first()
            assertEquals(testName, settings.general.userName)
        }

    @Test
    fun `updateSetting should work for boolean settings`() =
        runTest {
            settingsDataStore.updateSetting(GeneralSettingsRegistry.CENSOR_HABIT_NAMES, true)

            val settings = settingsDataStore.settingsFlow.first()
            assertTrue(settings.general.censorHabitNames)
        }

    @Test
    fun `updateSetting should work for int settings`() =
        runTest {
            val testIntSetting =
                SettingDefinition(
                    key = "test_int_settings",
                    defaultValue = 1,
                    type = SettingType.IntType,
                    displayName = "Test int key",
                )

            settingsDataStore.updateSetting(testIntSetting, 2)

            val preferences = testDataStore.data.first()
            assertEquals(2, preferences[intPreferencesKey(testIntSetting.key)])
        }

    @Test
    fun `updateSetting should work for long settings`() =
        runTest {
            settingsDataStore.updateSetting(PomodoroSettingsRegistry.WORK_TIME_IN_MS, 5000L)

            val settings = settingsDataStore.settingsFlow.first()
            assertEquals(5000L, settings.pomodoro.workTimeInMs)
        }

    @Test
    fun `updateSettings should work with GeneralSettings entity`() =
        runTest {
            val newGeneralSettings =
                createMockGeneralSettings(
                    userName = "Jane Smith",
                    censorHabitNames = true,
                    lockResetProgressButton = true,
                )

            settingsDataStore.updateSettings(newGeneralSettings)

            val settings = settingsDataStore.settingsFlow.first()
            assertEquals("Jane Smith", settings.general.userName)
            assertTrue(settings.general.censorHabitNames)
            assertTrue(settings.general.lockResetProgressButton)
        }

    @Test
    fun `updateSettings should work with PomodoroSettings entity`() =
        runTest {
            val newPomodoroSettings =
                createMockPomodoroSettings(
                    restTimeInMs = 10,
                    workTimeInMs = 45,
                )

            settingsDataStore.updateSettings(newPomodoroSettings)

            val settings = settingsDataStore.settingsFlow.first()
            assertEquals(10, settings.pomodoro.restTimeInMs)
            assertEquals(45, settings.pomodoro.workTimeInMs)
        }

    @Test
    fun `resetAllSettings should clear all preferences`() =
        runTest {
            // First set some values
            val generalSettings =
                createMockGeneralSettings(
                    userName = "Test User",
                    censorHabitNames = true,
                    lockResetProgressButton = true,
                )
            val pomodoroSettings =
                createMockPomodoroSettings(
                    restTimeInMs = 15,
                    workTimeInMs = 50,
                )

            settingsDataStore.updateSettings(generalSettings)
            settingsDataStore.updateSettings(pomodoroSettings)

            // Verify settings were applied
            var settings = settingsDataStore.settingsFlow.first()
            assertEquals("Test User", settings.general.userName)
            assertEquals(15, settings.pomodoro.restTimeInMs)

            // Reset all settings
            settingsDataStore.resetAllSettings()

            // Verify everything is back to defaults
            settings = settingsDataStore.settingsFlow.first()
            assertEquals("", settings.general.userName)
            assertFalse(settings.general.censorHabitNames)
            assertFalse(settings.general.lockResetProgressButton)
            assertEquals(5 * (1000 * 60), settings.pomodoro.restTimeInMs)
            assertEquals(25 * (1000 * 60), settings.pomodoro.workTimeInMs)
        }

    @Test
    fun `multiple concurrent updates should be handled correctly`() =
        runTest {
            // Simulate concurrent updates
            settingsDataStore.updateSetting(GeneralSettingsRegistry.USER_NAME, "Concurrent User")
            settingsDataStore.updateSetting(GeneralSettingsRegistry.CENSOR_HABIT_NAMES, true)
            settingsDataStore.updateSetting(PomodoroSettingsRegistry.WORK_TIME_IN_MS, 35)

            val settings = settingsDataStore.settingsFlow.first()

            assertEquals("Concurrent User", settings.general.userName)
            assertTrue(settings.general.censorHabitNames)
            assertEquals(35, settings.pomodoro.workTimeInMs)
        }

    @Test
    fun `settings flows should emit updates correctly`() =
        runTest {
            val generalSettings =
                createMockGeneralSettings(
                    userName = "Flow Test User",
                    censorHabitNames = false,
                    lockResetProgressButton = true,
                )

            settingsDataStore.updateSettings(generalSettings)

            // Test that both main flow and specific flow emit the update
            val mainSettings = settingsDataStore.settingsFlow.first()
            val generalFlow = settingsDataStore.generalSettingsFlow.first()

            assertEquals("Flow Test User", mainSettings.general.userName)
            assertEquals("Flow Test User", generalFlow.userName)
            assertTrue(mainSettings.general.lockResetProgressButton)
            assertTrue(generalFlow.lockResetProgressButton)
        }

    @Test
    fun `updateSettings should handle mixed types correctly`() =
        runTest {
            // Create settings with different types
            val mixedGeneralSettings =
                createMockGeneralSettings(
                    userName = "Mixed Types Test", // String
                    censorHabitNames = true, // Boolean
                    lockResetProgressButton = false, // Boolean
                )

            settingsDataStore.updateSettings(mixedGeneralSettings)

            val settings = settingsDataStore.settingsFlow.first()
            assertEquals("Mixed Types Test", settings.general.userName)
            assertTrue(settings.general.censorHabitNames)
            assertFalse(settings.general.lockResetProgressButton)
        }

    @Test
    fun `datastore should persist settings across recreations`() =
        runTest {
            val testUserName = "Persistence Test"

            settingsDataStore.updateSetting(GeneralSettingsRegistry.USER_NAME, testUserName)

            // Create a new instance with the same datastore
            val newSettingsDataStore =
                FakeSettingsDataStore(testDataStore)

            val settings = newSettingsDataStore.settingsFlow.first()
            assertEquals(testUserName, settings.general.userName)
        }

    // Helper functions to create mock settings
    private fun createMockGeneralSettings(
        userName: String = "",
        censorHabitNames: Boolean = false,
        lockResetProgressButton: Boolean = false,
    ): GeneralSettings {
        return GeneralSettings(
            userName = userName,
            censorHabitNames = censorHabitNames,
            lockResetProgressButton = lockResetProgressButton,
        )
    }

    private fun createMockPomodoroSettings(
        restTimeInMs: Long = 5 * (1000 * 60),
        workTimeInMs: Long = 25 * (1000 * 60),
    ): PomodoroSettings {
        return PomodoroSettings(
            restTimeInMs = restTimeInMs,
            workTimeInMs = workTimeInMs,
        )
    }
}
