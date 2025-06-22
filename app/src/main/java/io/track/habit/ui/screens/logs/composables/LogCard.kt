package io.track.habit.ui.screens.logs.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.track.habit.R
import io.track.habit.ui.composables.StreakCounter
import io.track.habit.ui.screens.logs.HabitLogWithStreak
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.PreviewMocks
import kotlin.random.Random

@Composable
fun LogCard(
    log: HabitLogWithStreak,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(13.dp),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(10.dp),
            ) {
                LogCardLabel(stringResource(R.string.streak)) {
                    LogCardStreak(streak = log)
                }

                LogCardLabel(stringResource(R.string.duration)) {
                    Text(text = log.dateDuration)
                }

                LogCardLabel(stringResource(R.string.trigger)) {
                    Text(text = log.log.trigger ?: stringResource(R.string.no_trigger_provided))
                }

                LogCardLabel(stringResource(R.string.notes)) {
                    Text(text = log.log.notes ?: stringResource(R.string.no_notes_provided))
                }
            }

            IconButton(
                onClick = onEdit,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    painter = painterResource(R.drawable.edit),
                    contentDescription = stringResource(R.string.edit_log_content_desc),
                    modifier = Modifier
                        .width(18.dp),
                )
            }
        }
    }
}

@Composable
private fun LogCardLabel(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = LocalContentColor.current.copy(0.6f),
        )

        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyMedium,
            content = content,
        )
    }
}

@Composable
private fun LogCardStreak(streak: HabitLogWithStreak) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StreakCounter(
            streak = streak.log.streakDuration,
            style = LocalTextStyle.current,
            iconSize = DpSize(12.dp, 12.dp),
            arrangement = Arrangement.spacedBy(4.dp),
        )

        HorizontalDivider(
            thickness = 1.dp,
            modifier = Modifier.width(3.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = streak.streak.badgeIcon.asPainter(),
                contentDescription = streak.streak.title.asString(),
                tint = Color.Unspecified,
                modifier =
                    Modifier
                        .width(12.dp)
                        .height(14.dp),
            )

            Text(text = streak.streak.title.asString())
        }
    }
}

@Preview
@Composable
private fun LogCardPreview() {
    TrackAHabitTheme {
        Surface {
            LogCard(
                log =
                    HabitLogWithStreak(
                        streak = PreviewMocks.getStreak(),
                        log =
                            PreviewMocks.getHabitLog(
                                streakDuration = remember { Random.nextInt(120, 1000) },
                            ),
                    ),
                onEdit = {},
            )
        }
    }
}
