package io.track.habit.ui.screens.habits.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.track.habit.R
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.model.Quote
import io.track.habit.ui.composables.StreakCounter
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.PreviewMocks
import io.track.habit.ui.utils.UiConstants
import io.track.habit.ui.utils.UiConstants.MEDIUM_EMPHASIS
import java.util.Date
import kotlin.random.Random

@Composable
fun HabitsScreenHeader(
    quote: Quote,
    isResetProgressButtonLocked: Boolean,
    isCensored: Boolean,
    habitWithStreak: HabitWithStreak,
    onEditHabit: () -> Unit,
    onDeleteHabit: () -> Unit,
    onViewLogs: () -> Unit,
    onResetProgress: () -> Unit,
    onToggleCensorship: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (_, streak) = habitWithStreak
    val habitName = habitWithStreak.getName(isCensored = isCensored)
    val formattedActiveSinceDate by remember {
        derivedStateOf {
            habitWithStreak.formattedActiveSinceDate
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(33.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start,
    ) {
        CommonLabel(text = stringResource(R.string.habit_name)) {
            HabitNameWithVisibilityToggle(
                habitName = habitName,
                isCensored = isCensored,
                onToggleCensorship = onToggleCensorship,
            )
        }

        CommonLabel(text = stringResource(R.string.streak_milestone)) {
            AnimatedContent(
                targetState = streak.title,
                label = "StreakTitle",
            ) {
                Text(
                    text = it,
                    style =
                        LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                )
            }
        }

        CommonLabel(text = stringResource(R.string.youve_been_on_track_for)) {
            AnimatedContent(
                targetState = habitWithStreak.habit.streakInDays,
                label = "StreakCounter",
            ) {
                StreakCounter(
                    streak = it,
                    iconSize = DpSize(24.dp, 31.dp),
                    style =
                        LocalTextStyle.current.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            shadow =
                                Shadow(
                                    color = Color.Black,
                                    offset = Offset(2f, 2f),
                                    blurRadius = 2f,
                                ),
                        ),
                )
            }

            AnimatedContent(
                targetState = formattedActiveSinceDate,
                label = "SinceDate",
            ) {
                Text(
                    text = stringResource(R.string.since_date_format, it),
                    style =
                        LocalTextStyle.current.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                        ),
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CommonButton(
                icon = painterResource(id = R.drawable.edit_colored),
                contentDescription = stringResource(R.string.edit_icon_content_desc),
                text = null,
                onClick = onEditHabit,
            )

            CommonButton(
                icon = painterResource(id = R.drawable.habit_logs),
                text = stringResource(R.string.logs),
                contentDescription = stringResource(R.string.habit_logs_icon_content_desc),
                onClick = onViewLogs,
            )

            CommonButton(
                icon = painterResource(id = R.drawable.sad_emoji),
                text = stringResource(R.string.reset_progress),
                contentDescription = stringResource(R.string.reset_progress_icon_content_desc),
                onClick = onResetProgress,
                enabled = isResetProgressButtonLocked,
            )

            CommonButton(
                icon = painterResource(id = R.drawable.delete_colored),
                contentDescription = stringResource(R.string.delete_icon_content_desc),
                text = stringResource(R.string.delete),
                onClick = onDeleteHabit,
            )
        }

        Text(
            text = quote.toString(),
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    color = LocalContentColor.current.copy(alpha = 0.8F),
                ),
        )
    }
}

@Composable
private fun CommonLabel(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start,
        modifier = modifier,
    ) {
        Text(
            text = text,
            style =
                LocalTextStyle.current.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalContentColor.current.copy(MEDIUM_EMPHASIS),
                ),
        )

        content()
    }
}

@Composable
private fun HabitNameWithVisibilityToggle(
    habitName: String,
    isCensored: Boolean,
    onToggleCensorship: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (icon, contentDesc) =
        if (isCensored) {
            painterResource(R.drawable.visibility) to stringResource(R.string.visibility_icon_content_desc)
        } else {
            painterResource(R.drawable.visibility_off) to stringResource(R.string.visibility_off_icon_content_desc)
        }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier.clickable {
                onToggleCensorship()
            },
    ) {
        AnimatedContent(
            targetState = habitName,
            label = "HabitName",
        ) {
            Text(
                text = it,
                style =
                    LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                    ),
            )
        }

        Icon(
            painter = icon,
            contentDescription = contentDesc,
            modifier =
                Modifier.clickable {
                    onToggleCensorship()
                },
        )
    }
}

@Composable
private fun CommonButton(
    icon: Painter,
    text: String?,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = Color.Unspecified,
        )

        text?.let {
            Text(
                text = it,
                modifier =
                    Modifier
                        .padding(start = 7.dp),
            )
        }
    }
}

@Preview
@Composable
private fun HabitsScreenHeaderPreview() {
    TrackAHabitTheme {
        Surface(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = UiConstants.ScreenPaddingHorizontal),
            ) {
                HabitsScreenHeader(
                    isResetProgressButtonLocked = false,
                    isCensored = false,
                    quote = PreviewMocks.getQuote(),
                    habitWithStreak =
                        HabitWithStreak(
                            habit =
                                PreviewMocks.getHabit(
                                    date =
                                        Date()
                                            .apply {
                                                val monthsToSubtract = Random.nextInt(0, 12)
                                                val monthsInMs = 1000 * 60 * 60 * 24 * 30L

                                                time -=
                                                    (monthsInMs * monthsToSubtract) +
                                                    (Random.nextInt(0, 99 * 1000 * 60 * 60))
                                            },
                                ),
                            streak = PreviewMocks.getStreak(),
                        ),
                    onEditHabit = {},
                    onDeleteHabit = {},
                    onViewLogs = {},
                    onResetProgress = {},
                    onToggleCensorship = {},
                )
            }
        }
    }
}
