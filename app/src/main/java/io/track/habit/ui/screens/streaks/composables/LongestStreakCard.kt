package io.track.habit.ui.screens.streaks.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.track.habit.R
import io.track.habit.ui.composables.StreakCounter
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.painterResourceFromString

@Composable
internal fun LongestStreakCard(
    streakCount: Int,
    title: String,
    dateStarted: String,
    streakTitle: String,
    streakBadge: String,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(41.dp),
            modifier =
                Modifier
                    .padding(14.dp),
        ) {
            Headline(
                title = title,
                streakTitle = streakTitle,
                streakBadge = streakBadge,
            )

            Footer(
                streakCount = streakCount,
                dateStarted = dateStarted,
            )
        }
    }
}

@Composable
private fun Headline(
    title: String,
    streakTitle: String,
    streakBadge: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(9.dp),
        modifier = modifier,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = LocalContentColor.current.copy(0.6f),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResourceFromString(streakBadge),
                contentDescription = streakTitle,
                tint = Color.Unspecified,
                modifier =
                    Modifier
                        .width(24.dp)
                        .height(24.dp),
            )

            Text(
                text = streakTitle,
                style =
                    LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
            )
        }
    }
}

@Composable
private fun Footer(
    streakCount: Int,
    dateStarted: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        StreakCounter(
            streak = streakCount,
            arrangement = Arrangement.spacedBy(4.dp),
            style =
                LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
            iconSize = DpSize(12.dp, 15.dp),
        )

        Text(
            text = stringResource(R.string.since_date_format, dateStarted),
            style =
                LocalTextStyle.current.copy(
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic
                )
        )
    }
}

@Preview
@Composable
private fun LongestStreakCardPreview() {
    TrackAHabitTheme {
        Surface {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                LongestStreakCard(
                    streakCount = 123,
                    title = stringResource(R.string.ongoing),
                    dateStarted = "2/2/2023",
                    streakTitle = "Grinner",
                    streakBadge = "grin_with_sweat_emoji",
                    modifier =
                        Modifier
                            .weight(0.5f),
                )

                LongestStreakCard(
                    streakCount = 421,
                    title = stringResource(R.string.all_time),
                    dateStarted = "1/4/2022",
                    streakTitle = "Notekeeper",
                    streakBadge = "habit_logs",
                    modifier =
                        Modifier
                            .weight(0.5f),
                )
            }
        }
    }
}
