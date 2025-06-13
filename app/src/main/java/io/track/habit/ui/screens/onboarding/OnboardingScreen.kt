package io.track.habit.ui.screens.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.track.habit.R
import io.track.habit.ui.screens.onboarding.composables.CustomEllipse
import io.track.habit.ui.screens.onboarding.composables.OnboardingHeadliner
import io.track.habit.ui.theme.TrackAHabitTheme

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.onNext()
            } else {
                val toastMessage =
                    Toast.makeText(
                        // context =
                        context,
                        // text =
                        context.getString(R.string.disabled_notifications),
                        // duration =
                        Toast.LENGTH_LONG,
                    )

                toastMessage.show()
            }
        }

    OnboardingScreenContent(
        step = { uiState.currentStep },
        username = { uiState.username },
        requestPermissions = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val isPermissionGranted =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    )

                when (isPermissionGranted) {
                    PackageManager.PERMISSION_GRANTED -> {}
                    else -> launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        },
        onUsernameChange = viewModel::onUsernameChange,
        onNext = viewModel::onNext,
        onBack = viewModel::onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingScreenContent(
    step: () -> OnboardingStep,
    username: () -> String,
    requestPermissions: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isStepThreeWithEmptyUsername by remember {
        derivedStateOf {
            step() is OnboardingStep.Step3 && username().trim().isEmpty()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
    ) {
        CustomEllipse()

        TopAppBar(
            title = {},
            windowInsets = WindowInsets(0.dp),
            navigationIcon = {
                AnimatedVisibility(
                    visible = step() !is OnboardingStep.Step1,
                    enter = slideInHorizontally(),
                ) {
                    TextButton(
                        onClick = onBack,
                        colors =
                            ButtonDefaults.textButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onPrimary.copy(0.6f),
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.back),
                        )

                        Text(
                            text = stringResource(R.string.back),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(
                modifier = Modifier.fillMaxHeight(0.1f),
            )

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
            ) {
                OnboardingHeadliner(
                    step = step,
                    modifier =
                        Modifier
                            .fillMaxWidth(0.85f)
                            .weight(1f),
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedContent(
                        targetState = step,
                    ) {
                        when (it.invoke()) {
                            OnboardingStep.Step2 ->
                                NotificationPermissionStep(
                                    requestPermissions = requestPermissions,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 16.dp),
                                )

                            OnboardingStep.Step3 ->
                                UsernameInputStep(
                                    username = username,
                                    onUsernameChange = onUsernameChange,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 16.dp),
                                )

                            else -> Unit
                        }
                    }

                    IconButton(
                        onClick = onNext,
                        enabled = !isStepThreeWithEmptyUsername,
                        modifier =
                            Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isStepThreeWithEmptyUsername) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                ).size(55.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.next),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(35.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationPermissionStep(
    requestPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hasPermissions =
        remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth(),
    ) {
        FilledTonalButton(
            onClick = requestPermissions,
            enabled = !hasPermissions,
            colors =
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text =
                    if (hasPermissions) {
                        stringResource(R.string.allowed_notifications)
                    } else {
                        stringResource(R.string.settings_notifications)
                    },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun UsernameInputStep(
    username: () -> String,
    onUsernameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isError = username().trim().isEmpty()

    OutlinedTextField(
        value = username(),
        onValueChange = onUsernameChange,
        label = { Text(stringResource(R.string.username)) },
        placeholder = { Text(stringResource(R.string.settings_username_desc)) },
        singleLine = true,
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = stringResource(R.string.error_empty_field),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorSupportingTextColor = MaterialTheme.colorScheme.error,
            ),
        modifier = modifier.fillMaxWidth(),
    )
}

private fun Context.getAllRequiredPermissions(): List<String> {
    val requiredPermissions = mutableListOf<String>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    return requiredPermissions.filterNot { permission ->
        checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    val steps =
        remember {
            listOf(
                OnboardingStep.Step1,
                OnboardingStep.Step2,
                OnboardingStep.Step3,
                OnboardingStep.Step4,
            )
        }
    var index by remember { mutableIntStateOf(0) }
    var userName by remember { mutableStateOf("") }

    TrackAHabitTheme {
        Surface {
            OnboardingScreenContent(
                step = { steps[index % steps.size] },
                onBack = { --index },
                onNext = { ++index },
                onUsernameChange = { userName = it },
                requestPermissions = {},
                username = { userName },
            )
        }
    }
}
