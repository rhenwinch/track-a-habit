package io.track.habit.ui.screens.onboarding

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.track.habit.R
import io.track.habit.data.local.datastore.entities.UserAppStateRegistry
import io.track.habit.di.IoDispatcher
import io.track.habit.domain.datastore.SettingsDataStore
import io.track.habit.domain.utils.StringResource
import io.track.habit.domain.utils.stringRes
import io.track.habit.ui.utils.DrawableResource
import io.track.habit.ui.utils.drawableRes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val settingsDataStore: SettingsDataStore,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val ioScope = CoroutineScope(ioDispatcher)

        private val _uiState = MutableStateFlow(OnboardingUiState())
        val uiState = _uiState.asStateFlow()

        private var updateJob: Job? = null
        private var onboardingJob: Job? = null

        fun onNext() {
            if (_uiState.value.usernameError || onboardingJob?.isActive == true) {
                return
            }

            onboardingJob =
                ioScope.launch {
                    val newStep = _uiState.value.goToNextStep()

                    if (newStep != null) {
                        _uiState.value = _uiState.value.copy(currentStep = newStep)
                    } else if (updateJob?.isActive == false) {
                        updateJob =
                            launch {
                                settingsDataStore.updateSetting(
                                    definition = UserAppStateRegistry.IS_FIRST_RUN,
                                    value = false,
                                )
                            }
                    }
                }
        }

        fun onBack() {
            if (onboardingJob?.isActive == true) {
                return
            }

            onboardingJob =
                ioScope.launch {
                    val newStep = _uiState.value.goToPreviousStep()
                    if (newStep != null) {
                        _uiState.value = _uiState.value.copy(currentStep = newStep)
                    }
                }
        }

        fun onUsernameChange(newUsername: String) {
            _uiState.value = _uiState.value.copy(username = newUsername)
        }
    }

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.Step1,
    val username: String = "",
) {
    private val steps: List<OnboardingStep> =
        listOf(
            OnboardingStep.Step1,
            OnboardingStep.Step2,
            OnboardingStep.Step3,
            OnboardingStep.Step4,
        )

    private val isFirstStep: Boolean get() = currentStep == steps.first()

    private val isLastStep: Boolean get() = currentStep == steps.last()

    val usernameError get() = currentStep is OnboardingStep.Step3 && username.trim().isEmpty()

    fun goToNextStep(): OnboardingStep? {
        return if (!isLastStep) {
            steps[steps.indexOf(currentStep) + 1]
        } else {
            null
        }
    }

    fun goToPreviousStep(): OnboardingStep? {
        return if (!isFirstStep) {
            steps[steps.indexOf(currentStep) - 1]
        } else {
            null
        }
    }
}

sealed interface OnboardingStep {
    val icon: DrawableResource
    val headline: StringResource
    val subText: StringResource

    @Stable
    data object Step1 : OnboardingStep {
        override val icon: DrawableResource = drawableRes(R.drawable.onboarding_step1)
        override val headline: StringResource = stringRes(R.string.onboarding_step1_headline)
        override val subText: StringResource = stringRes(R.string.onboarding_step1_subtext)
    }

    @Stable
    data object Step2 : OnboardingStep {
        override val icon: DrawableResource = drawableRes(R.drawable.onboarding_step2)
        override val headline: StringResource = stringRes(R.string.onboarding_step2_headline)
        override val subText: StringResource = stringRes(R.string.onboarding_step2_subtext)
    }

    @Stable
    data object Step3 : OnboardingStep {
        override val icon: DrawableResource = drawableRes(R.drawable.onboarding_step3)
        override val headline: StringResource = stringRes(R.string.onboarding_step3_headline)
        override val subText: StringResource = stringRes(R.string.onboarding_step3_subtext)
    }

    @Stable
    data object Step4 : OnboardingStep {
        override val icon: DrawableResource = drawableRes(R.drawable.onboarding_step4)
        override val headline: StringResource = stringRes(R.string.onboarding_step4_headline)
        override val subText: StringResource = stringRes(R.string.onboarding_step4_subtext)
    }
}
