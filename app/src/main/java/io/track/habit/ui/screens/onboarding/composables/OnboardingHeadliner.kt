package io.track.habit.ui.screens.onboarding.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.track.habit.R
import io.track.habit.ui.screens.onboarding.OnboardingStep
import io.track.habit.ui.theme.TrackAHabitTheme
import kotlinx.coroutines.delay

@Composable
fun OnboardingHeadliner(
    step: () -> OnboardingStep,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onPrimary,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(27.dp, Alignment.Top),
            horizontalAlignment = Alignment.End,
            modifier = modifier,
        ) {
            // Image with bounce effect animation
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    // Custom transition for the image - slide in from right with bounce
                    ContentTransform(
                        targetContentEnter =
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec =
                                    SpringSpec(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow,
                                    ),
                            ) + fadeIn(animationSpec = tween(300)),
                        initialContentExit =
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(300),
                            ) + fadeOut(animationSpec = tween(200)),
                        sizeTransform = SizeTransform(clip = false),
                    )
                },
                label = "Image Animation",
                modifier = Modifier.fillMaxWidth(0.4f),
            ) { currentStep ->
                Image(
                    painter = currentStep().icon.asPainter(),
                    contentDescription = stringResource(R.string.onboarding_step_icon_content_desc),
                )
            }

            // Headline with slide in from left animation
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    // Delay by offsetting the animation duration
                    val enterDelay = 150
                    val exitDuration = 250

                    ContentTransform(
                        targetContentEnter =
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(durationMillis = 400, delayMillis = enterDelay),
                            ) + fadeIn(animationSpec = tween(400, delayMillis = enterDelay)),
                        initialContentExit =
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(exitDuration),
                            ) + fadeOut(animationSpec = tween(exitDuration)),
                        sizeTransform = SizeTransform(clip = false),
                    )
                },
                label = "Headline Animation",
                modifier = Modifier.fillMaxWidth(),
            ) { currentStep ->
                Text(
                    text = currentStep().headline.asString(),
                    style =
                        LocalTextStyle.current.copy(
                            fontSize = 36.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Right,
                        ),
                )
            }

            // SubText with combination of slide and expand animation
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    // Further delay with longer animation duration
                    val enterDelay = 250
                    val exitDuration = 200

                    ContentTransform(
                        targetContentEnter =
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth / 2 },
                                animationSpec = tween(durationMillis = 350, delayMillis = enterDelay),
                            ) +
                                expandVertically(
                                    expandFrom = Alignment.Top,
                                    animationSpec = tween(350, delayMillis = enterDelay),
                                ) + fadeIn(animationSpec = tween(350, delayMillis = enterDelay)),
                        initialContentExit =
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth / 2 },
                                animationSpec = tween(exitDuration),
                            ) + fadeOut(animationSpec = tween(exitDuration)),
                        sizeTransform = SizeTransform(clip = false),
                    )
                },
                label = "SubText Animation",
                modifier = Modifier.fillMaxWidth(),
            ) { currentStep ->
                Text(
                    text = currentStep().subText.asString(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Right,
                )
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingHeadlinerPreview() {
    // Create a state to cycle through the different steps
    val steps =
        listOf(
            OnboardingStep.Step1,
            OnboardingStep.Step2,
            OnboardingStep.Step3,
            OnboardingStep.Step4,
        )
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]

    // Use LaunchedEffect to cycle through steps every 3 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Wait for 3 seconds before changing step
            currentStepIndex = (currentStepIndex + 1) % steps.size
        }
    }

    TrackAHabitTheme {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
        ) {
            OnboardingHeadliner(step = { currentStep })
        }
    }
}
