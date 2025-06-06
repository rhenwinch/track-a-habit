package io.track.habit.ui.screens.habits

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.track.habit.R
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.model.Quote
import io.track.habit.domain.utils.SortOrder
import io.track.habit.ui.composables.AlertDialog
import io.track.habit.ui.screens.habits.composables.FilterBottomSheet
import io.track.habit.ui.screens.habits.composables.HabitCard
import io.track.habit.ui.screens.habits.composables.HabitOptionsSheet
import io.track.habit.ui.screens.habits.composables.HabitsScreenHeader
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.PreviewMocks
import io.track.habit.ui.utils.UiConstants
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HabitsScreen(
    modifier: Modifier = Modifier,
    viewModel: HabitsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val isCensoringHabitNames by viewModel.isCensoringHabitNames.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = uiState.isInitialized,
        label = "HabitsScreenLoadingState",
        modifier = modifier
    ) { isInitialized ->
        if (!isInitialized) {
            InitializationScreen()
        } else {
            HabitsScreenContent(
                username = username,
                habits = habits,
                habitIdToShow = uiState.habitIdToShow,
                quote = uiState.quote,
                longPressedHabit = uiState.longPressedHabit,
                isCensoringHabitNames = isCensoringHabitNames,
                onHabitClick = viewModel::toggleShowcaseHabit,
                onDeleteHabit = viewModel::deleteHabit,
                onToggleCensorship = { viewModel.toggleCensorshipOnNames(!isCensoringHabitNames) },
                onHabitLongClick = viewModel::onHabitLongClick,
                onResetProgress = viewModel::resetProgress,
                sortOrder = uiState.sortOrder,
                onSortOrderSelect = viewModel::onSortOrderSelect,
                onEditHabit = {
                    // Handle edit habit action
                    // This could open a dialog or navigate to an edit screen
                    // TODO
                },
                onViewLogs = {
                    // Handle view logs action
                    // Navigation or dialog would be handled here
                    // TODO
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreenContent(
    username: String,
    habits: List<HabitWithStreak>,
    habitIdToShow: Long,
    quote: Quote,
    longPressedHabit: HabitWithStreak?,
    isCensoringHabitNames: Boolean,
    sortOrder: SortOrder,
    onSortOrderSelect: (SortOrder) -> Unit,
    onHabitClick: (HabitWithStreak) -> Unit,
    onHabitLongClick: (HabitWithStreak?) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onViewLogs: (Habit) -> Unit,
    onResetProgress: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onToggleCensorship: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showResetProgressDialog by rememberSaveable { mutableStateOf(false) }
    var showSortSheet by rememberSaveable { mutableStateOf(false) }

    AnimatedContent(
        targetState = habits.isNotEmpty(),
        modifier = modifier.fillMaxSize(),
    ) {
        if (it) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "${getTimeOfDayGreeting()}, $username",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style =
                                    LocalTextStyle.current.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )
                        },
                        actions = {
                            IconButton(onClick = { showSortSheet = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.filter),
                                    contentDescription = stringResource(R.string.filter_icon_content_desc),
                                )
                            }
                        },
                    )
                },
            ) { innerPadding ->
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(300.dp),
                    contentPadding =
                        PaddingValues(
                            horizontal = UiConstants.ScreenPaddingHorizontal,
                            vertical = innerPadding.calculateTopPadding(),
                        ),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        val habitToShow = habits[habitIdToShow.toInt()]

                        HabitsScreenHeader(
                            quote = quote,
                            isCensored = isCensoringHabitNames,
                            habitWithStreak = habitToShow,
                            onEditHabit = { onEditHabit(habitToShow.habit) },
                            onDeleteHabit = { showDeleteDialog = true },
                            onViewLogs = { onViewLogs(habitToShow.habit) },
                            onResetProgress = { showResetProgressDialog = true },
                            onToggleCensorship = onToggleCensorship,
                            modifier = Modifier.padding(vertical = UiConstants.ScreenPaddingHorizontal),
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 22.dp))
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = stringResource(R.string.my_other_habits),
                            style =
                                LocalTextStyle.current.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalContentColor.current.copy(alpha = 0.6f),
                                ),
                        )
                    }

                    items(
                        habits,
                        key = { key -> key.habit.habitId },
                    ) { habitWithStreak ->
                        HabitCard(
                            habitWithStreak = habitWithStreak,
                            onClick = { onHabitClick(habitWithStreak) },
                            onLongClick = { onHabitLongClick(habitWithStreak) },
                            modifier =
                                Modifier
                                    .padding(vertical = 5.dp)
                                    .animateItem(),
                        )
                    }
                }
            }
        } else {
            EmptyDataScreen()
        }
    }

    if (showDeleteDialog) {
        val habitToShow = habits[habitIdToShow.toInt()]

        AlertDialog(
            dialogTitle = stringResource(R.string.delete_habit),
            dialogText = stringResource(R.string.delete_habit_confirmation),
            onDismissRequest = { showDeleteDialog = false },
            onConfirmation = {
                onDeleteHabit(habitToShow.habit)
                showDeleteDialog = false
            },
        )
    }

    if (showResetProgressDialog) {
        val habitToShow = habits[habitIdToShow.toInt()]

        AlertDialog(
            dialogTitle = stringResource(R.string.reset_progress),
            dialogText = stringResource(R.string.reset_progress_confirmation),
            onDismissRequest = { showResetProgressDialog = false },
            onConfirmation = {
                onResetProgress(habitToShow.habit)
                showResetProgressDialog = false
            },
        )
    }

    if (showSortSheet) {
        val sheetState = rememberModalBottomSheetState()

        FilterBottomSheet(
            currentSortOrder = sortOrder,
            onSortOrderSelect = onSortOrderSelect,
            sheetState = sheetState,
            onDismiss = {
                scope
                    .launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showSortSheet = false
                        }
                    }
            },
        )
    }

    if (longPressedHabit != null) {
        val sheetState = rememberModalBottomSheetState()

        HabitOptionsSheet(
            onEditClick = { onEditHabit(longPressedHabit.habit) },
            onDeleteClick = { onDeleteHabit(longPressedHabit.habit) },
            sheetState = sheetState,
            onDismiss = {
                scope
                    .launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onHabitLongClick(null)
                        }
                    }
            },
        )
    }
}

@Composable
private fun getTimeOfDayGreeting(): String {
    val calendar = Calendar.getInstance()
    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

    return when (hourOfDay) {
        in 0..11 -> stringResource(R.string.good_morning)
        in 12..17 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    }
}

@Composable
private fun EmptyDataScreen() {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .padding(UiConstants.ScreenPaddingHorizontal)
                .fillMaxSize(),
    ) {
        Text(
            text = "\uD83E\uDEE5",
            style =
                LocalTextStyle.current.copy(
                    fontSize = 100.sp,
                ),
        )

        Text(
            text = stringResource(R.string.empty_title),
            style =
                LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalContentColor.current.copy(alpha = 0.8f),
                ),
            modifier =
                Modifier
                    .padding(top = 16.dp),
        )

        Text(
            text = stringResource(R.string.empty_habits_message),
            modifier = Modifier.fillMaxWidth(0.85F),
            style =
                LocalTextStyle.current.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = LocalContentColor.current.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                ),
        )
    }
}

@Composable
private fun InitializationScreen() {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(UiConstants.ScreenPaddingHorizontal),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp),
            strokeCap = StrokeCap.Round
        )

        Text(
            text = stringResource(R.string.loading_message),
            style =
                LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = LocalContentColor.current.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                ),
            modifier = Modifier
                .fillMaxWidth(0.85F)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HabitsScreenPreview() {
    TrackAHabitTheme {
        Surface {
            val previewHabits =
                List(5) { index ->
                    HabitWithStreak(
                        habit =
                            PreviewMocks.getHabit(
                                suffix = index.toString(),
                                habitId = index.toLong(),
                            ),
                        streak = PreviewMocks.getStreak(suffix = index.toString()),
                    )
                }

            HabitsScreenContent(
                username = "Preview User",
                habits = previewHabits,
                habitIdToShow = 0,
                quote = PreviewMocks.getQuote(),
                isCensoringHabitNames = false,
                longPressedHabit = null,
                sortOrder = SortOrder.Name(true),
                onHabitClick = {},
                onHabitLongClick = {},
                onEditHabit = {},
                onDeleteHabit = {},
                onViewLogs = {},
                onResetProgress = {},
                onToggleCensorship = {},
                onSortOrderSelect = {},
            )
        }
    }
}

@Preview
@Composable
private fun HabitsScreenLoadingPreview() {
    TrackAHabitTheme {
        Surface {
            InitializationScreen()
        }
    }
}
