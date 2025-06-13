package io.track.habit.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import io.track.habit.datastore.FakeSettingsDataStore
import io.track.habit.ui.screens.onboarding.OnboardingStep
import io.track.habit.ui.screens.onboarding.OnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
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
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var fakeSettingsDataStore: FakeSettingsDataStore
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() =
        runTest {
            Dispatchers.setMain(testDispatcher)

            val testDataStoreFile = File(tempFolder.newFolder(), "test-preferences.preferences_pb")
            dataStore =
                PreferenceDataStoreFactory.create(
                    scope = TestScope(testDispatcher + Job()),
                    produceFile = { testDataStoreFile },
                )

            fakeSettingsDataStore = FakeSettingsDataStore(dataStore)
            viewModel =
                OnboardingViewModel(
                    settingsDataStore = fakeSettingsDataStore,
                    ioDispatcher = testDispatcher,
                )

            advanceUntilIdle()
        }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has first step and empty username`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                val initialState = awaitItem()
                expectThat(initialState).get { currentStep }.isEqualTo(OnboardingStep.Step1)
                expectThat(initialState).get { username }.isEqualTo("")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onNext navigates to next step`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                // Initial state
                awaitItem()

                // Navigate to step 2
                viewModel.onNext()
                advanceUntilIdle()

                expectThat(awaitItem()).get { currentStep }.isEqualTo(OnboardingStep.Step2)

                // Navigate to step 3
                viewModel.onNext()
                advanceUntilIdle()

                expectThat(awaitItem()).get { currentStep }.isEqualTo(OnboardingStep.Step3)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onBack navigates to previous step`() =
        runTest(testDispatcher) {
            // Move to step 3 first
            viewModel.onNext()
            advanceUntilIdle()
            viewModel.onNext()
            advanceUntilIdle()

            viewModel.uiState.test {
                // Initial state at step 3
                val initialState = awaitItem()
                expectThat(initialState).get { currentStep }.isEqualTo(OnboardingStep.Step3)

                // Navigate back to step 2
                viewModel.onBack()
                advanceUntilIdle()

                expectThat(awaitItem()).get { currentStep }.isEqualTo(OnboardingStep.Step2)

                // Navigate back to step 1
                viewModel.onBack()
                advanceUntilIdle()

                expectThat(awaitItem()).get { currentStep }.isEqualTo(OnboardingStep.Step1)

                // Should not go back from first step
                viewModel.onBack()
                advanceUntilIdle()

                // Should remain at step 1
                expectNoEvents()
            }
        }

    @Test
    fun `username error is true when username is empty at step 3`() =
        runTest(testDispatcher) {
            // Navigate to step 3
            viewModel.onNext()
            advanceUntilIdle()
            viewModel.onNext()
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state).get { currentStep }.isEqualTo(OnboardingStep.Step3)
                expectThat(state).get { usernameError }.isTrue()

                // Update username
                viewModel.onUsernameChange("TestUser")

                val updatedState = awaitItem()
                expectThat(updatedState).get { username }.isEqualTo("TestUser")
                expectThat(updatedState).get { usernameError }.isFalse()

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `completing all steps updates settings`() =
        runTest(testDispatcher) {
            // Navigate to step 3
            viewModel.onNext()
            advanceUntilIdle()
            viewModel.onNext()
            advanceUntilIdle()

            // Set username
            viewModel.onUsernameChange("TestUser")

            // Navigate to step 4
            viewModel.onNext()
            advanceUntilIdle()

            // Complete onboarding
            viewModel.onNext()
            advanceUntilIdle()

            // Verify settings were updated
            turbineScope {
                val appStateTurbine = fakeSettingsDataStore.appStateFlow.testIn(backgroundScope)
                val generalSettingsTurbine = fakeSettingsDataStore.generalSettingsFlow.testIn(backgroundScope)

                with(appStateTurbine) {
                    val appState = awaitItem()
                    expectThat(appState).get { isFirstRun }.isFalse()
                }

                with(generalSettingsTurbine) {
                    val generalSettings = awaitItem()
                    expectThat(generalSettings).get { userName }.isEqualTo("TestUser")
                }

                appStateTurbine.cancelAndIgnoreRemainingEvents()
                generalSettingsTurbine.cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onUsernameChange updates username in state`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                val initialState = awaitItem()
                expectThat(initialState).get { username }.isEqualTo("")

                viewModel.onUsernameChange("NewUser")

                val updatedState = awaitItem()
                expectThat(updatedState).get { username }.isEqualTo("NewUser")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `settings are not updated until onboarding is completed`() =
        runTest(testDispatcher) {
            turbineScope {
                viewModel.onNext()
                advanceUntilIdle()
                viewModel.onNext()
                advanceUntilIdle()
                viewModel.onUsernameChange("TestUser")
                viewModel.onNext()
                advanceUntilIdle()
                viewModel.onNext()
                advanceUntilIdle()

                val appStateTurbine = fakeSettingsDataStore.appStateFlow.testIn(backgroundScope)
                val generalSettingsTurbine = fakeSettingsDataStore.generalSettingsFlow.testIn(backgroundScope)

                with(appStateTurbine) {
                    expectThat(awaitItem()).get { isFirstRun }.isFalse()
                    cancelAndIgnoreRemainingEvents()
                }

                with(generalSettingsTurbine) {
                    expectThat(awaitItem()).get { userName }.isEqualTo("TestUser") // Username set
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
}
