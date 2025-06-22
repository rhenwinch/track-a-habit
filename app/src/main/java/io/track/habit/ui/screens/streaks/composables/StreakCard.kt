package io.track.habit.ui.screens.streaks.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.track.habit.ui.screens.streaks.StreakSummary
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.PreviewMocks
import io.track.habit.ui.utils.painterResourceFromString

@Composable
fun StreakCard(
    onClick: () -> Unit,
    enabled: Boolean,
    streakSummary: StreakSummary,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.heightIn(min = 40.dp),
        onClick = onClick,
        enabled = enabled,
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
                    verticalAlignment = Alignment.Top,
                    modifier =
                        Modifier
                            .weight(0.5f)
                            .padding(end = 8.dp),
                ) {
                    Icon(
                        painter = painterResourceFromString(streakSummary.badgeIcon),
                        contentDescription = streakSummary.title.asString(),
                        tint = if (streakSummary.isAchieved) Color.Unspecified else LocalContentColor.current,
                        modifier =
                            Modifier
                                .width(20.dp)
                                .height(26.dp),
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = streakSummary.title.asString(),
                            style =
                                LocalTextStyle.current.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )

                        Text(
                            text = streakSummary.status.asString(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Text(
                    text = streakSummary.durationText.asString(),
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LocalContentColor.current.copy(0.6f),
                        ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun StreakCardPreview() {
    TrackAHabitTheme {
        Surface {
            StreakCard(
                onClick = {},
                enabled = true,
                streakSummary =
                    PreviewMocks.getStreakSummary(
                        streak =
                            PreviewMocks.getStreak().copy(
                                title = "Trailblazer",
                            ),
                    ),
            )
        }
    }
}
