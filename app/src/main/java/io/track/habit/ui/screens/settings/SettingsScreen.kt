package io.track.habit.ui.screens.settings

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.track.habit.R
import io.track.habit.data.local.datastore.entities.GeneralSettings
import io.track.habit.data.local.datastore.entities.GeneralSettingsRegistry
import io.track.habit.data.remote.drive.AuthorizationState
import io.track.habit.domain.datastore.SettingDefinition
import io.track.habit.domain.model.BackupFile
import io.track.habit.domain.utils.stringLiteral
import io.track.habit.ui.screens.settings.composables.SettingItem
import io.track.habit.ui.theme.TrackAHabitTheme

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val generalSettings by viewModel.general.collectAsStateWithLifecycle()
    val isSignedIn by viewModel.isSignedIn.collectAsStateWithLifecycle()
    val lastBackupDate by viewModel.lastBackupDate.collectAsStateWithLifecycle()
    val backupOperationState by viewModel.backupOperationState.collectAsStateWithLifecycle()
    val availableBackups by viewModel.availableBackups.collectAsStateWithLifecycle()
    val authorizationState by viewModel.authorizationState.collectAsStateWithLifecycle()

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

    SettingsScreenContent(
        generalSettings = generalSettings,
        isSignedIn = isSignedIn,
        lastBackupDate = lastBackupDate,
        backupOperationState = backupOperationState,
        authorizationState = authorizationState,
        availableBackups = availableBackups,
        snackbarHostState = snackbarHostState,
        onSettingChange = { definition, value ->
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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showRestoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                onRestore = { backupId ->
                    onRestoreClick(backupId)
                    showRestoreDialog = false
                },
                onDeleteBackup = onDeleteBackupClick,
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

            // Show authorization error if present
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
                // User is signed in - show backup options
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
                    // Backup button
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

                    // Restore button
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

                // Sign out button
                TextButton(
                    onClick = onSignOutClick,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(text = stringResource(id = R.string.sign_out))
                }
            } else {
                // User is not signed in - show sign in button
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
    onRestore: (String) -> Unit,
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
                    items(backups) { backup ->
                        BackupItem(
                            backup = backup,
                            onRestore = { onRestore(backup.id) },
                            onDelete = { onDeleteBackup(backup.id) },
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

            // Add extra padding at the bottom for better UX
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BackupItem(
    backup: BackupFile,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 8.dp),
        ) {
            // Display backup filename with better formatting
            Text(
                text = backup.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Extract and display date from filename if possible
            backup.name.substringAfter("backup_").substringBefore(".db").let { dateString ->
                if (dateString.contains("-")) {
                    val formattedDate = dateString.replace("-", "/").replace("_", " ")
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }

            // Action buttons in a row at the bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Restore button
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

                // Delete button
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
