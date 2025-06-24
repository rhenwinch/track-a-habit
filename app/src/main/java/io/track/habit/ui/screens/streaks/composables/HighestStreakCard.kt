package io.track.habit.ui.screens.streaks.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.track.habit.R
import io.track.habit.domain.model.AllTimeStreak
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.utils.drawableRes
import io.track.habit.domain.utils.formatActiveSinceDate
import io.track.habit.domain.utils.stringLiteral
import io.track.habit.ui.composables.StreakCounter
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.PreviewMocks
import java.util.Date

/**
 * For highest ongoing streaks
 * */
@Composable
fun HighestStreakCard(
    highestOngoingStreak: HabitWithStreak?,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier) {
        AnimatedContent(
            targetState = highestOngoingStreak,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
        ) {
            if (it == null) {
                NeedMoreDataCard()
            } else {
                HighestStreakCardContent(
                    streakCount = it.habit.streakInDays,
                    title = stringResource(R.string.ongoing),
                    dateStarted =
                        stringResource(
                            R.string.since_date_format,
                            it.habit.lastResetAt.formatActiveSinceDate(),
                        ),
                    streakTitle = it.streak.title.asString(),
                    streakBadge = it.streak.badgeIcon.asPainter()
                )
            }
        }
    }
}

/**
 * For highest all-time streaks
 * */
@Composable
fun HighestStreakCard(
    highestAllTimeStreak: AllTimeStreak?,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier) {
        AnimatedContent(
            targetState = highestAllTimeStreak,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
        ) {
            if (it == null) {
                NeedMoreDataCard()
            } else {
                HighestStreakCardContent(
                    streakCount = it.streakInDays,
                    title = stringResource(R.string.all_time),
                    dateStarted = it.formattedDateDuration,
                    streakTitle = it.streak.title.asString(),
                    streakBadge = it.streak.badgeIcon.asPainter(),
                )
            }
        }
    }
}

@Composable
private fun HighestStreakCardContent(
    streakCount: Int,
    title: String,
    dateStarted: String,
    streakTitle: String,
    streakBadge: Painter,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(14.dp)
            .heightIn(152.dp),
    ) {
        Headline(
            title = title,
            streakTitle = streakTitle,
            streakBadge = streakBadge,
        )

        Spacer(Modifier.height(16.dp))

        Footer(
            streakCount = streakCount,
            date = dateStarted,
        )
    }
}

@Composable
private fun NeedMoreDataCard(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxHeight(),
    ) {
        Text(
            text = stringResource(R.string.insufficient_data_for_stats),
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalContentColor.current.copy(0.8f),
                    textAlign = TextAlign.Center,
                ),
            modifier =
                Modifier
                    .padding(horizontal = 12.dp),
        )
    }
}

@Composable
private fun Headline(
    title: String,
    streakTitle: String,
    streakBadge: Painter,
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
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = streakBadge,
                contentDescription = streakTitle,
                tint = Color.Unspecified,
                modifier =
                    Modifier
                        .width(24.dp)
                        .height(24.dp),
            )

            Text(
                text = streakTitle,
                overflow = TextOverflow.Ellipsis,
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
    date: String,
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
            text = date,
            style =
                LocalTextStyle.current.copy(
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                ),
        )
    }
}

@Preview
@Composable
private fun HighestStreakCardPreview() {
    val allTimeStreak =
        AllTimeStreak(
            streakInDays = 240,
            endDate = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 240L),
            startDate = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 420L),
            streak =
                PreviewMocks.getStreak().copy(
                    title = stringLiteral("Centennial Gemstone AhahahaAHHAHAHAHAHAHAEAATEATEAT"),
                    badgeIcon = drawableRes(R.drawable.badge_gemstone),
                ),
        )

    TrackAHabitTheme {
        Surface {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                HighestStreakCard(
                    highestOngoingStreak = null,
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                )

                HighestStreakCard(
                    highestAllTimeStreak = allTimeStreak,
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun NeedMoreDataPreview() {
    TrackAHabitTheme {
        Surface {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                NeedMoreDataCard(
                    modifier =
                        Modifier
                            .weight(0.5f),
                )
                NeedMoreDataCard(
                    modifier =
                        Modifier
                            .weight(0.5f),
                )
            }
        }
    }
}
