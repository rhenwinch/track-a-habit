package io.track.habit.ui.screens.habits.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.track.habit.R
import io.track.habit.data.local.database.entities.Habit.Companion.getName
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.model.Quote
import io.track.habit.ui.composables.GradientFireIcon
import io.track.habit.ui.composables.GradientText
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.FireGradientGenerator
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
    val (habit, streak) = habitWithStreak
    val habitName = habitWithStreak.habit.getName(isCensored = isCensored)
    val formattedActiveSinceDate = remember(habitWithStreak.habit.habitId) { habitWithStreak.formattedActiveSinceDate }
    val formattedDurationSinceReset =
        remember(habitWithStreak.habit.habitId) { habitWithStreak.formattedDurationSinceReset }

    Box(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopStart),
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
                    targetState = streak.title.asString(),
                    transitionSpec = {
                        slideInHorizontally { -it } + fadeIn() togetherWith fadeOut() + slideOutHorizontally { -it }
                    },
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
                    targetState = formattedDurationSinceReset,
                    transitionSpec = {
                        slideInHorizontally { -it } + fadeIn() togetherWith fadeOut() + slideOutHorizontally { -it }
                    },
                    label = "StreakCounter",
                ) {
                    val streakGradient = FireGradientGenerator.getGradient(habit.streakInDays)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GradientFireIcon(
                            painter = painterResource(R.drawable.streak_filled),
                            contentDescription = stringResource(R.string.streak_icon_content_desc),
                            modifier = Modifier.size(24.dp, 31.dp),
                            gradient = streakGradient,
                        )

                        GradientText(
                            text = it,
                            gradient = streakGradient,
                            style = LocalTextStyle.current.copy(
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
                }

                AnimatedContent(
                    targetState = formattedActiveSinceDate,
                    transitionSpec = {
                        slideInHorizontally { -it } + fadeIn() togetherWith fadeOut() + slideOutHorizontally { -it }
                    },
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
                    enabled = !isResetProgressButtonLocked,
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

        AnimatedContent(
            targetState = habitWithStreak.streak.badgeIcon.asPainter(),
            label = "Icon Animation",
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith fadeOut() + slideOutHorizontally { it }
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(0.3f)
                .align(Alignment.TopEnd),
        ) {
            Image(
                painter = it,
                contentDescription = habitWithStreak.streak.title.asString(),
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .aspectRatio(1f),
            )
        }
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
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .clickable {
                onToggleCensorship()
            },
    ) {
        AnimatedContent(
            targetState = habitName,
            label = "HabitName",
            transitionSpec = {
                slideInHorizontally { -it } + fadeIn() togetherWith fadeOut() + slideOutHorizontally { -it }
            },
            modifier = Modifier
                .weight(0.4f, fill = false),
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

        Box(
            modifier = Modifier
                .weight(0.4f, fill = false),
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDesc,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable {
                        onToggleCensorship()
                    },
            )
        }
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
private fun HabitsScreenHeaderPreview(name: String = "Habit Name") {
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
                                PreviewMocks
                                    .getHabit(
                                        date =
                                            Date()
                                                .apply {
                                                    val monthsToSubtract = Random.nextInt(0, 12)
                                                    val monthsInMs = 1000 * 60 * 60 * 24 * 30L

                                                    time -=
                                                        (monthsInMs * monthsToSubtract) +
                                                        (Random.nextInt(0, 99 * 1000 * 60 * 60))
                                                },
                                    ).copy(name = name),
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

@Preview
@Composable
private fun HabitsScreenHeaderPreview2() {
    HabitsScreenHeaderPreview(name = "Drink Water and Stay Hydrated and Healthy")
}
