package io.track.habit.ui.screens.streaks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.track.habit.R
import io.track.habit.domain.model.AllTimeStreak
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.ui.screens.streaks.composables.HighestStreakCard
import io.track.habit.ui.screens.streaks.composables.StatsLabel
import io.track.habit.ui.screens.streaks.composables.StreakCard
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.PreviewMocks
import io.track.habit.ui.utils.UiConstants
import java.util.Date

@Composable
fun StreaksScreen(viewModel: StreakViewModel = hiltViewModel()) {
    val highestOngoingStreak by viewModel.highestOngoingStreak.collectAsStateWithLifecycle()
    val highestAllTimeStreak by viewModel.highestAllTimeStreak.collectAsStateWithLifecycle()
    val streakSummaries by viewModel.streakSummaries.collectAsStateWithLifecycle()

    StreaksScreenContent(
        highestOngoingStreak = highestOngoingStreak,
        highestAllTimeStreak = highestAllTimeStreak,
        streakSummaries = streakSummaries,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreaksScreenContent(
    highestOngoingStreak: HabitWithStreak?,
    highestAllTimeStreak: AllTimeStreak?,
    streakSummaries: List<StreakSummary>,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = stringResource(R.string.streaks),
                        style =
                            LocalTextStyle.current.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .padding(horizontal = UiConstants.ScreenPaddingHorizontal),
        ) {
            item {
                StatsLabel(
                    title = stringResource(R.string.longest_streaks),
                    description = stringResource(R.string.longest_streaks_desc),
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    HighestStreakCard(
                        highestOngoingStreak = highestOngoingStreak,
                        modifier = Modifier.weight(0.5f),
                    )

                    HighestStreakCard(
                        highestAllTimeStreak = highestAllTimeStreak,
                        modifier = Modifier.weight(0.5f),
                    )
                }
            }

            item {
                StatsLabel(
                    title = stringResource(R.string.streak_milestones),
                    description = stringResource(R.string.streak_milestones_desc),
                    modifier = Modifier.padding(top = 25.dp, bottom = 11.dp),
                )
            }

            items(streakSummaries) { summary ->
                StreakCard(
                    streakSummary = summary,
                    modifier = Modifier.padding(vertical = 5.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun StreaksScreenPreview() {
    val highestOngoingStreak =
        HabitWithStreak(
            habit = PreviewMocks.getHabit(date = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 240L)),
            streak = PreviewMocks.getStreak(),
        )

    val allTimeStreak =
        AllTimeStreak(
            streakInDays = 240,
            endDate = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 240L),
            startDate = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 420L),
            streak =
                PreviewMocks.getStreak().copy(
                    title = "Grinner",
                    badgeIcon = "grin_with_sweat_emoji",
                ),
        )

    val streakSummaries =
        remember {
            List(10) {
                PreviewMocks.getStreakSummary(
                    streak = PreviewMocks.getStreak(suffix = it.toString()),
                )
            }
        }

    TrackAHabitTheme {
        Surface {
            StreaksScreenContent(
                highestOngoingStreak = highestOngoingStreak,
                highestAllTimeStreak = allTimeStreak,
                streakSummaries = streakSummaries,
            )
        }
    }
}
