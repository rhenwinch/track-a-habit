package io.track.habit.ui.screens.logs

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import io.track.habit.data.local.database.entities.HabitLog
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.PreviewMocks
import kotlin.random.Random

@Composable
fun LogsScreen() {
}

@Composable
fun LogsScreenContent(
    logs: List<HabitLogWithStreak>,
    onUpdateLog: (HabitLog) -> Unit,
) {
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
            )
        }
    }
}
