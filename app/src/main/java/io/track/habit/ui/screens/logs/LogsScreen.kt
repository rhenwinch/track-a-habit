package io.track.habit.ui.screens.logs

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.track.habit.R
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.ui.composables.EmptyDataScreen
import io.track.habit.ui.screens.logs.composables.EditLogDialog
import io.track.habit.ui.screens.logs.composables.LogCard
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.PreviewMocks
import io.track.habit.ui.utils.UiConstants
import kotlin.random.Random

@Composable
fun LogsScreen(
    viewModel: LogsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    LogsScreenContent(
        logs = logs,
        onUpdateLog = viewModel::updateLog,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreenContent(
    logs: List<HabitLogWithStreak>,
    onUpdateLog: (HabitLog) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    var logToEdit by remember { mutableStateOf<HabitLog?>(null) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.logs)) },
                windowInsets = WindowInsets(0.dp),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = logs.isEmpty(),
            modifier =
                Modifier
                    .padding(innerPadding),
        ) { isTrue ->
            if (isTrue) {
                EmptyDataScreen(
                    message = stringResource(R.string.empty_logs_message),
                    icon = painterResource(R.drawable.you_rock_emoji),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier =
                        Modifier
                            .padding(horizontal = UiConstants.ScreenPaddingHorizontal),
                ) {
                    items(logs) {
                        LogCard(
                            log = it,
                            onEdit = { logToEdit = it.log },
                        )
                    }
                }
            }
        }
    }

    if (logToEdit != null) {
        EditLogDialog(
            habitLog = logToEdit!!,
            onDismissRequest = { logToEdit = null },
            onConfirm = { updatedLog ->
                onUpdateLog(updatedLog)
                logToEdit = null
            },
        )
    }
}

@Preview
@Composable
private fun LogsScreenPreview() {
    TrackAHabitTheme {
        Surface {
            LogsScreenContent(
                logs =
                    listOf(
                        HabitLogWithStreak(
                            streak = PreviewMocks.getStreak(),
                            log =
                                PreviewMocks.getHabitLog(
                                    streakDuration = remember { Random.nextInt(120, 1000) },
                                ),
                        ),
                    ),
                onUpdateLog = {},
                onNavigateBack = {},
            )
        }
    }
}

@Preview
@Composable
private fun LogsScreenEmptyPreview() {
    TrackAHabitTheme {
        Surface {
            LogsScreenContent(
                logs = listOf(),
                onUpdateLog = {},
                onNavigateBack = {},
            )
        }
    }
}
