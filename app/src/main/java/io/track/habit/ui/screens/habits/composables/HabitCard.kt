package io.track.habit.ui.screens.habits.composables

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
import io.track.habit.data.local.database.entities.Habit.Companion.getName
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.ui.composables.StreakCounter
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.PreviewMocks
import io.track.habit.ui.utils.painterResourceFromString
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
internal fun HabitCard(
    habitWithStreak: HabitWithStreak,
    isCensored: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(),
        border = CardDefaults.outlinedCardBorder(),
        modifier =
            modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                StreakCounter(
                    streak = habitWithStreak.habit.streakInDays,
                    iconSize = DpSize(22.dp, 27.dp),
                    style =
                        LocalTextStyle.current.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            shadow =
                                Shadow(
                                    color = Color.Black,
                                    offset = Offset(2f, 2f),
                                    blurRadius = 2f,
                                ),
                        ),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResourceFromString(habitWithStreak.streak.badgeIcon),
                        contentDescription = stringResource(R.string.streak_icon_content_desc),
                        tint = Color.Unspecified,
                        modifier = Modifier.width(18.dp)
                    )

                    Text(
                        text = habitWithStreak.streak.title,
                        style =
                            LocalTextStyle.current.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right,
                            ),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = habitWithStreak.habit.getName(isCensored = isCensored),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        LocalTextStyle.current.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                        ),
                )

                Text(
                    text = habitWithStreak.habit.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        LocalTextStyle.current.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Right,
                        ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun HabitCardBasePreview() {
    TrackAHabitTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
            ) {
                items(20) {
                    val date =
                        remember {
                            val dateString = "2023-07-" + (1 + it)
                            val format = SimpleDateFormat("yyyy-MM-d", Locale.getDefault())

                            format.parse(dateString)!!
                        }

                    HabitCard(
                        isCensored = false,
                        habitWithStreak =
                            HabitWithStreak(
                                habit =
                                    PreviewMocks.getHabit(
                                        date = date,
                                        suffix = it.toString(),
                                    ),
                                streak =
                                    PreviewMocks.getStreak(
                                        suffix = it.toString(),
                                    ),
                            ),
                        onClick = {},
                        onLongClick = {},
                    )
                }
            }
        }
    }
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
private fun HabitCardCompactLandscapePreview() {
    HabitCardBasePreview()
}

@Preview(device = "spec:parent=medium_tablet,orientation=portrait")
@Composable
private fun HabitCardMediumPortraitPreview() {
    HabitCardBasePreview()
}

@Preview(device = "spec:parent=medium_tablet,orientation=landscape")
@Composable
private fun HabitCardMediumLandscapePreview() {
    HabitCardBasePreview()
}

@Preview(device = "spec:width=1920dp,height=1080dp,dpi=160,orientation=portrait")
@Composable
private fun HabitCardExtendedPortraitPreview() {
    HabitCardBasePreview()
}

@Preview(device = "spec:width=1920dp,height=1080dp,dpi=160,orientation=landscape")
@Composable
private fun HabitCardExtendedLandscapePreview() {
    HabitCardBasePreview()
}
