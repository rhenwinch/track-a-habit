package io.track.habit.ui.screens.habits

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import io.track.habit.ui.screens.habits.composables.EditHabitDialog
import io.track.habit.ui.screens.habits.composables.FilterBottomSheet
import io.track.habit.ui.screens.habits.composables.HabitCard
import io.track.habit.ui.screens.habits.composables.HabitOptionsSheet
import io.track.habit.ui.screens.habits.composables.HabitsScreenHeader
import io.track.habit.ui.screens.habits.composables.ResetDetails
import io.track.habit.ui.screens.habits.composables.ResetProgressDialog
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.PreviewMocks
import io.track.habit.ui.utils.UiConstants
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HabitsScreen(
    onViewLogs: (Habit) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = uiState.isInitialized,
        label = "HabitsScreenLoadingState",
        modifier = modifier,
    ) { isInitialized ->
        if (!isInitialized) {
            InitializationScreen()
        } else {
            val habits by viewModel.habits.collectAsStateWithLifecycle()
            val username by viewModel.username.collectAsStateWithLifecycle()
            val isResetProgressButtonLocked by viewModel.isResetProgressButtonLocked.collectAsStateWithLifecycle()

            HabitsScreenContent(
                username = username,
                habits = habits,
                habitToShowcase = uiState.habitToShowcase,
                quote = uiState.quote,
                longPressedHabit = uiState.longPressedHabit,
                sortOrder = uiState.sortOrder,
                isResetProgressButtonLocked = isResetProgressButtonLocked,
                isCensoringHabitNames = uiState.isCensoringHabitNames,
                onHabitClick = viewModel::toggleShowcaseHabit,
                onDeleteHabit = viewModel::deleteHabit,
                onToggleCensorship = { viewModel.toggleCensorshipOnNames(!uiState.isCensoringHabitNames) },
                onHabitLongClick = viewModel::onHabitLongClick,
                onResetProgress = viewModel::resetProgress,
                onSortOrderSelect = viewModel::onSortOrderSelect,
                onEditHabit = viewModel::updateHabit,
                onViewLogs = onViewLogs,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreenContent(
    username: String,
    habits: List<HabitWithStreak>,
    habitToShowcase: HabitWithStreak?,
    quote: Quote,
    longPressedHabit: HabitWithStreak?,
    isResetProgressButtonLocked: Boolean,
    isCensoringHabitNames: Boolean,
    sortOrder: SortOrder,
    onSortOrderSelect: (SortOrder) -> Unit,
    onHabitClick: (HabitWithStreak) -> Unit,
    onHabitLongClick: (HabitWithStreak?) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onViewLogs: (Habit) -> Unit,
    onResetProgress: (ResetDetails) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onToggleCensorship: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showResetProgressDialog by rememberSaveable { mutableStateOf(false) }
    var showSortSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(habitToShowcase) {
        gridState.animateScrollToItem(0)
    }

    Scaffold(
        modifier =
            modifier.fillMaxSize(),
        topBar = {
            AnimatedVisibility(
                visible = habits.isNotEmpty(),
                label = "HabitsScreenTopAppBar",
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${getTimeOfDayGreeting()}, $username",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
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
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = habits.isNotEmpty(),
        ) {
            if (it) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(300.dp),
                    state = gridState,
                    contentPadding =
                        PaddingValues(
                            horizontal = UiConstants.ScreenPaddingHorizontal,
                            vertical = innerPadding.calculateTopPadding(),
                        ),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        if (habitToShowcase != null) {
                            HabitsScreenHeader(
                                quote = quote,
                                isResetProgressButtonLocked = isResetProgressButtonLocked,
                                isCensored = isCensoringHabitNames,
                                habitWithStreak = habitToShowcase,
                                onEditHabit = { showEditDialog = true },
                                onDeleteHabit = { showDeleteDialog = true },
                                onViewLogs = { onViewLogs(habitToShowcase.habit) },
                                onResetProgress = { showResetProgressDialog = true },
                                onToggleCensorship = onToggleCensorship,
                                modifier = Modifier.padding(vertical = UiConstants.ScreenPaddingHorizontal),
                            )
                        }
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

                    if (habits.size == 1) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyDataScreen()
                        }
                    } else {
                        items(
                            habits,
                            key = { key -> key.habit.habitId },
                        ) { habitWithStreak ->
                            if (habitWithStreak != habitToShowcase) {
                                HabitCard(
                                    habitWithStreak = habitWithStreak,
                                    isCensored = isCensoringHabitNames,
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
                }
            } else {
                EmptyDataScreen()
            }
        }
    }

    if (habitToShowcase != null) {
        if (showEditDialog) {
            EditHabitDialog(
                initialHabitName = habitToShowcase.getName(isCensoringHabitNames),
                onDismissRequest = { showEditDialog = false },
                onSaveClick = { updatedHabit ->
                    onEditHabit(habitToShowcase.habit.copy(name = updatedHabit))
                    showEditDialog = false
                },
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                dialogTitle = stringResource(R.string.delete_habit),
                dialogText = stringResource(R.string.delete_habit_confirmation),
                onDismissRequest = { showDeleteDialog = false },
                onConfirmation = {
                    onDeleteHabit(habitToShowcase.habit)
                    showDeleteDialog = false
                },
            )
        }

        if (showResetProgressDialog) {
            ResetProgressDialog(
                onConfirm = {
                    onResetProgress(it)
                    showResetProgressDialog = false
                },
                onDismissRequest = { showResetProgressDialog = false },
                habitId = habitToShowcase.habit.habitId,
            )
        }
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
                .heightIn(400.dp)
                .fillMaxSize(),
    ) {
        Icon(
            painter = painterResource(R.drawable.grin_with_sweat_emoji),
            contentDescription = stringResource(R.string.empty_data_icon_content_desc),
            modifier = Modifier.size(100.dp),
            tint = Color.Unspecified,
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
            strokeCap = StrokeCap.Round,
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
            modifier =
                Modifier
                    .fillMaxWidth(0.85F),
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
                habitToShowcase = previewHabits[0],
                quote = PreviewMocks.getQuote(),
                isResetProgressButtonLocked = true,
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

@Preview
@Composable
private fun HabitsScreenEmptyDataPreview() {
    TrackAHabitTheme {
        Surface {
            EmptyDataScreen()
        }
    }
}
