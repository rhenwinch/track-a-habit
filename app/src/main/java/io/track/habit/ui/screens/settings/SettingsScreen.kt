package io.track.habit.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.track.habit.R
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.local.datastore.entities.GeneralSettingsRegistry
import io.track.habit.data.remote.backup.RemoteBackupManager
import io.track.habit.data.remote.drive.AuthorizationState
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.model.BackupFile
import io.track.habit.domain.utils.stringLiteral
import io.track.habit.ui.screens.settings.composables.SettingItem
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.authenticate
import io.track.habit.ui.utils.getBiometricsPromptInfo
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val generalSettings by viewModel.general.collectAsStateWithLifecycle()
    val isSignedIn by viewModel.isSignedIn.collectAsStateWithLifecycle()
    val lastBackupDate by viewModel.lastBackupDate.collectAsStateWithLifecycle()
    val backupOperationState by viewModel.backupOperationState.collectAsStateWithLifecycle()
    val availableBackups by viewModel.availableBackups.collectAsStateWithLifecycle()
    val authorizationState by viewModel.authorizationState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle backup operation state changes
    LaunchedEffect(backupOperationState) {
        when (backupOperationState) {
            is BackupOperationState.Success -> {
                val result = snackbarHostState.showSnackbar(
                    (backupOperationState as BackupOperationState.Success).message.asString(context),
                )
                // Reset state after snackbar is shown and consumed
                if (result == SnackbarResult.Dismissed || result == SnackbarResult.ActionPerformed) {
                    viewModel.resetBackupOperationState()
                }
            }

            is BackupOperationState.Error -> {
                val result = snackbarHostState.showSnackbar(
                    (backupOperationState as BackupOperationState.Error).message.asString(context),
                )
                // Reset state after snackbar is shown and consumed
                if (result == SnackbarResult.Dismissed || result == SnackbarResult.ActionPerformed) {
                    viewModel.resetBackupOperationState()
                }
            }

            else -> {}
        }
    }

    // Handle authorization state errors separately
    LaunchedEffect(authorizationState) {
        if (authorizationState is AuthorizationState.Error) {
            snackbarHostState.showSnackbar((authorizationState as AuthorizationState.Error).message.asString(context))
        }
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.updateSettingWithCast(GeneralSettingsRegistry.NOTIFICATIONS_ENABLED, true)
            }
        }

    SettingsScreenContent(
        generalSettings = generalSettings,
        isSignedIn = isSignedIn,
        lastBackupDate = lastBackupDate,
        backupOperationState = backupOperationState,
        authorizationState = authorizationState,
        availableBackups = availableBackups,
        snackbarHostState = snackbarHostState,
        onSettingChange = { definition, value ->
            if (definition == GeneralSettingsRegistry.NOTIFICATIONS_ENABLED && value as Boolean) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val isPermissionGranted =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        )

                    if (isPermissionGranted != PackageManager.PERMISSION_GRANTED) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        return@SettingsScreenContent
                    }
                }
            }

            if (definition == GeneralSettingsRegistry.CENSOR_HABIT_NAMES && !(value as Boolean)) {
                val biometricsPromptInfo = getBiometricsPromptInfo(
                    title = context.getString(R.string.biometrics_prompt_title),
                    subtitle = context.getString(R.string.biometrics_prompt_subtitle),
                    negativeButtonText = context.getString(R.string.biometrics_prompt_fallback),
                )

                context.authenticate(
                    prompt = biometricsPromptInfo,
                    onAuthSucceed = { viewModel.updateSettingWithCast(definition, value) },
                    onAuthFailed = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.biometrics_auth_failed),
                                withDismissAction = true,
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
                )
                return@SettingsScreenContent
            }

            viewModel.updateSettingWithCast(definition, value)
        },
        onSignInClick = viewModel::signInToGoogleDrive,
        onSignOutClick = viewModel::signOutFromGoogleDrive,
        onBackupClick = viewModel::createBackup,
        onRestoreClick = viewModel::restoreFromBackup,
        onDeleteBackupClick = viewModel::deleteBackup,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    generalSettings: GeneralSettings,
    isSignedIn: Boolean,
    lastBackupDate: String?,
    backupOperationState: BackupOperationState,
    authorizationState: AuthorizationState,
    availableBackups: List<BackupFile>,
    snackbarHostState: SnackbarHostState,
    onSettingChange: (definition: SettingDefinition<*>, value: Any) -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: (String) -> Unit,
    onDeleteBackupClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackupForRestore by remember { mutableStateOf<BackupFile?>(null) }
    var showRestoreConfirmationDialog by remember { mutableStateOf(false) }
    var showRestartRequiredDialog by remember { mutableStateOf(false) }

    LaunchedEffect(backupOperationState) {
        if (backupOperationState is BackupOperationState.Success &&
            backupOperationState.message.asString(context) == context.getString(R.string.success_restore_completed)
        ) {
            showRestartRequiredDialog = true
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = {
                    Text(
                        text = stringResource(id = R.string.settings),
                        style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Google Drive Backup section
            GoogleDriveBackupSection(
                isSignedIn = isSignedIn,
                lastBackupDate = lastBackupDate,
                backupOperationState = backupOperationState,
                authorizationState = authorizationState,
                onSignInClick = onSignInClick,
                onSignOutClick = onSignOutClick,
                onBackupClick = onBackupClick,
                onRestoreClick = { showRestoreDialog = true },
            )

            HorizontalDivider()

            // General settings section
            Text(
                text = stringResource(id = R.string.general_settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val settingDefinitions = GeneralSettingsRegistry.getAllSettings()

                settingDefinitions.forEach { definition ->
                    when (definition) {
                        GeneralSettingsRegistry.USER_NAME -> {
                            SettingItem(
                                definition = definition,
                                currentValue = generalSettings.userName,
                                onValueChange = {
                                    onSettingChange(definition, it)
                                },
                            )
                        }

                        GeneralSettingsRegistry.CENSOR_HABIT_NAMES -> {
                            SettingItem(
                                definition = definition,
                                currentValue = generalSettings.censorHabitNames,
                                onValueChange = { onSettingChange(definition, it) },
                            )
                        }

                        GeneralSettingsRegistry.LOCK_RESET_PROGRESS -> {
                            SettingItem(
                                definition = definition,
                                currentValue = generalSettings.lockResetProgressButton,
                                onValueChange = { onSettingChange(definition, it) },
                            )
                        }

                        GeneralSettingsRegistry.NOTIFICATIONS_ENABLED -> {
                            SettingItem(
                                definition = definition,
                                currentValue = generalSettings.notificationsEnabled,
                                onValueChange = { onSettingChange(definition, it) },
                            )
                        }
                    }
                }
            }
        }

        // Restore Dialog
        if (showRestoreDialog) {
            BackupRestoreDialog(
                backups = availableBackups,
                onDismiss = { showRestoreDialog = false },
                onRestore = { backupFile ->
                    selectedBackupForRestore = backupFile
                    showRestoreConfirmationDialog = true
                },
                onDeleteBackup = onDeleteBackupClick,
            )
        }

        // Confirmation dialog before restore
        if (showRestoreConfirmationDialog && selectedBackupForRestore != null) {
            AlertDialog(
                onDismissRequest = { showRestoreConfirmationDialog = false },
                title = {
                    Text(
                        text = stringResource(id = R.string.confirm_restore_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Column {
                        Text(stringResource(id = R.string.confirm_restore_message))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedBackupForRestore!!.name.substringBefore(
                                RemoteBackupManager.BACKUP_FILE_EXTENSION,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onRestoreClick(selectedBackupForRestore!!.id)
                            showRestoreConfirmationDialog = false
                            showRestoreDialog = false
                        },
                    ) {
                        Text(stringResource(id = R.string.proceed))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showRestoreConfirmationDialog = false
                        },
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                },
            )
        }

        if (showRestartRequiredDialog) {
            AlertDialog(
                onDismissRequest = { /* Do nothing */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
                title = {
                    Text(
                        text = stringResource(id = R.string.restart_required_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Text(stringResource(id = R.string.restart_required_message))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                            intent?.addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                    Intent.FLAG_ACTIVITY_NEW_TASK,
                            )
                            context.startActivity(intent)
                            Runtime.getRuntime().exit(0)
                        },
                    ) {
                        Text(stringResource(id = R.string.restart))
                    }
                },
            )
        }

        // Loading indicator for various states
        if (backupOperationState == BackupOperationState.SigningIn ||
            backupOperationState == BackupOperationState.BackingUp ||
            backupOperationState == BackupOperationState.Restoring ||
            backupOperationState == BackupOperationState.Deleting ||
            authorizationState == AuthorizationState.Authorizing
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun GoogleDriveBackupSection(
    isSignedIn: Boolean,
    lastBackupDate: String?,
    backupOperationState: BackupOperationState,
    authorizationState: AuthorizationState,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(id = R.string.google_drive_backup),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            if (authorizationState is AuthorizationState.Error) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = authorizationState.message.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (isSignedIn) {
                Text(
                    text = stringResource(id = R.string.connected_to_google_drive),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                // Last backup info
                if (lastBackupDate != null) {
                    Text(
                        text = stringResource(id = R.string.last_backup, lastBackupDate),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onBackupClick,
                        modifier = Modifier.weight(1f),
                        enabled = backupOperationState == BackupOperationState.Idle ||
                            backupOperationState is BackupOperationState.Success ||
                            backupOperationState is BackupOperationState.Error,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.backup),
                            contentDescription = stringResource(id = R.string.backup_now),
                            modifier = Modifier.padding(end = 4.dp),
                        )

                        Text(text = stringResource(id = R.string.backup_now))
                    }

                    OutlinedButton(
                        onClick = onRestoreClick,
                        modifier = Modifier.weight(1f),
                        enabled = backupOperationState == BackupOperationState.Idle ||
                            backupOperationState is BackupOperationState.Success ||
                            backupOperationState is BackupOperationState.Error,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.restore),
                            contentDescription = stringResource(id = R.string.restore),
                            modifier = Modifier.padding(end = 4.dp),
                        )

                        Text(text = stringResource(id = R.string.restore))
                    }
                }

                TextButton(
                    onClick = onSignOutClick,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(text = stringResource(id = R.string.sign_out))
                }
            } else {
                Text(
                    text = stringResource(id = R.string.google_drive_backup_description),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSignInClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = backupOperationState == BackupOperationState.Idle ||
                        backupOperationState is BackupOperationState.Success ||
                        backupOperationState is BackupOperationState.Error,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.gdrive),
                        contentDescription = stringResource(id = R.string.sign_in_with_google),
                        tint = Color.Unspecified,
                        modifier = Modifier.padding(end = 4.dp),
                    )

                    Text(text = stringResource(id = R.string.sign_in_with_google))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupRestoreDialog(
    backups: List<BackupFile>,
    onDismiss: () -> Unit,
    onRestore: (BackupFile) -> Unit,
    onDeleteBackup: (String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.select_backup_to_restore),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (backups.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.no_backups_available),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        backups,
                        key = { it.id },
                    ) { backup ->
                        BackupItem(
                            backup = backup,
                            onRestore = { onRestore(backup) },
                            onDelete = { onDeleteBackup(backup.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BackupItem(
    backup: BackupFile,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 8.dp),
        ) {
            Text(
                text = backup.name.substringBefore(RemoteBackupManager.BACKUP_FILE_EXTENSION),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            backup.name
                .substringAfter(RemoteBackupManager.BACKUP_FILE_PREFIX)
                .substringBefore(RemoteBackupManager.BACKUP_FILE_EXTENSION)
                .let { dateString ->
                    if (dateString.contains("-")) {
                        val formattedDate = dateString.replace("-", "/").replace("_", " ")
                        val formattedTime = formattedDate.substringAfterLast(" ").replace("/", ":")

                        Text(
                            text = "${formattedDate.substringBefore(" ")} $formattedTime",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onRestore,
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.restore),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(id = R.string.restore),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                TextButton(
                    onClick = onDelete,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete_backup_content_desc),
                        modifier = Modifier.padding(end = 8.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = stringResource(id = R.string.delete),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    TrackAHabitTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsScreenContent(
                generalSettings = GeneralSettings(
                    userName = "John Doe",
                    censorHabitNames = true,
                    lockResetProgressButton = false,
                    notificationsEnabled = true,
                ),
                isSignedIn = true,
                lastBackupDate = "2025-06-17 15:30:22",
                backupOperationState = BackupOperationState.Idle,
                authorizationState = AuthorizationState.Authorized,
                availableBackups = listOf(
                    BackupFile("id1", "backup_2025-06-17_15-30-22.db"),
                    BackupFile("id2", "backup_2025-06-16_10-15-33.db"),
                ),
                snackbarHostState = SnackbarHostState(),
                onSettingChange = { _, _ -> },
                onSignInClick = { },
                onSignOutClick = { },
                onBackupClick = { },
                onRestoreClick = { },
                onDeleteBackupClick = { },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenWithAuthorizationErrorPreview() {
    TrackAHabitTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsScreenContent(
                generalSettings = GeneralSettings(
                    userName = "John Doe",
                    censorHabitNames = true,
                    lockResetProgressButton = false,
                    notificationsEnabled = true,
                ),
                isSignedIn = false,
                lastBackupDate = null,
                backupOperationState = BackupOperationState.Idle,
                authorizationState = AuthorizationState.Error(stringLiteral("Toast authorization error")),
                availableBackups = emptyList(),
                snackbarHostState = SnackbarHostState(),
                onSettingChange = { _, _ -> },
                onSignInClick = { },
                onSignOutClick = { },
                onBackupClick = { },
                onRestoreClick = { },
                onDeleteBackupClick = { },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GoogleDriveBackupSectionSignedInPreview() {
    TrackAHabitTheme {
        Surface {
            GoogleDriveBackupSection(
                isSignedIn = true,
                lastBackupDate = "2025-06-17 15:30:22",
                backupOperationState = BackupOperationState.Idle,
                authorizationState = AuthorizationState.Authorized,
                onSignInClick = { },
                onSignOutClick = { },
                onBackupClick = { },
                onRestoreClick = { },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GoogleDriveBackupSectionSignedOutPreview() {
    TrackAHabitTheme {
        Surface {
            GoogleDriveBackupSection(
                isSignedIn = false,
                lastBackupDate = null,
                backupOperationState = BackupOperationState.Idle,
                authorizationState = AuthorizationState.NotAuthorized,
                onSignInClick = { },
                onSignOutClick = { },
                onBackupClick = { },
                onRestoreClick = { },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BackupRestoreDialogPreview() {
    TrackAHabitTheme {
        BackupRestoreDialog(
            backups = listOf(
                BackupFile("id1", "backup_2025-06-17_15-30-22.db"),
                BackupFile("id2", "backup_2025-06-16_10-15-33.db"),
                BackupFile("id3", "backup_2025-06-15_08-45-12.db"),
            ),
            onDismiss = { },
            onRestore = { },
            onDeleteBackup = { },
        )
    }
}
