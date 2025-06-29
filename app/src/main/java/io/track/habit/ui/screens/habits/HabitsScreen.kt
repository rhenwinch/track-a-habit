package io.track.habit.ui.screens.habits

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.gson.Gson
import io.track.habit.R
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.domain.model.HabitWithStreak
import io.track.habit.domain.model.Quote
import io.track.habit.domain.utils.SortOrder
import io.track.habit.domain.utils.StringResource
import io.track.habit.ui.composables.AlertDialog
import io.track.habit.ui.composables.EmptyDataScreen
import io.track.habit.ui.screens.create.CREATE_HABIT_KEY
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
import io.track.habit.ui.utils.authenticate
import io.track.habit.ui.utils.getBiometricsPromptInfo
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HabitsScreen(
    onViewLogs: (Habit) -> Unit,
    onAddHabit: () -> Unit,
    savedStateHandle: SavedStateHandle?,
    modifier: Modifier = Modifier,
    viewModel: HabitsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var isAuthenticatedOnceForDeleteOrResetProgress by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    if (savedStateHandle != null) {
        val gson = remember { Gson() }
        val createdHabit by savedStateHandle
            .getStateFlow<String?>(CREATE_HABIT_KEY, null)
            .collectAsStateWithLifecycle()

        LaunchedEffect(createdHabit) {
            createdHabit?.let { habitJson ->
                val habit = runCatching {
                    gson.fromJson(habitJson, Habit::class.java)
                }.getOrNull()

                if (habit != null) {
                    viewModel.addHabit(habit)
                    savedStateHandle[CREATE_HABIT_KEY] = null // Clear the state after handling
                }
            }
        }
    }

    val biometricsPromptInfo = remember {
        getBiometricsPromptInfo(
            title = context.getString(R.string.biometrics_prompt_title),
            subtitle = context.getString(R.string.biometrics_prompt_subtitle),
            negativeButtonText = context.getString(R.string.biometrics_prompt_fallback),
        )
    }

    fun onAuthFailure() {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.biometrics_auth_failed),
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
        }
    }

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
                errorMessage = uiState.errorMessage,
                isResetProgressButtonLocked = isResetProgressButtonLocked,
                isCensoringHabitNames = uiState.isCensoringHabitNames,
                snackbarHostState = snackbarHostState,
                onHabitClick = viewModel::toggleShowcaseHabit,
                onHabitLongClick = viewModel::onHabitLongClick,
                onSortOrderSelect = viewModel::onSortOrderSelect,
                onEditHabit = viewModel::updateHabit,
                clearErrorMessage = viewModel::clearErrorMessage,
                onViewLogs = onViewLogs,
                onAddHabit = onAddHabit,
                onDeleteHabit = { habit ->
                    if (!isAuthenticatedOnceForDeleteOrResetProgress) {
                        context.authenticate(
                            prompt = biometricsPromptInfo,
                            onAuthSucceed = {
                                isAuthenticatedOnceForDeleteOrResetProgress = true
                                viewModel.deleteHabit(habit)
                            },
                            onAuthFailed = ::onAuthFailure,
                        )
                    } else {
                        viewModel.deleteHabit(habit)
                    }
                },
                onResetProgress = { habit ->
                    if (!isAuthenticatedOnceForDeleteOrResetProgress) {
                        context.authenticate(
                            prompt = biometricsPromptInfo,
                            onAuthSucceed = {
                                isAuthenticatedOnceForDeleteOrResetProgress = true
                                viewModel.resetProgress(habit)
                            },
                            onAuthFailed = ::onAuthFailure,
                        )
                    } else {
                        viewModel.resetProgress(habit)
                    }
                },
                onToggleCensorship = {
                    val isCensored = !uiState.isCensoringHabitNames
                    if (!isCensored) {
                        context.authenticate(
                            prompt = biometricsPromptInfo,
                            onAuthSucceed = { viewModel.toggleCensorshipOnNames(isCensored) },
                            onAuthFailed = ::onAuthFailure,
                        )
                    } else {
                        viewModel.toggleCensorshipOnNames(isCensored)
                    }
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
    habitToShowcase: HabitWithStreak?,
    quote: Quote,
    longPressedHabit: HabitWithStreak?,
    errorMessage: StringResource?,
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
    onAddHabit: () -> Unit,
    clearErrorMessage: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    // Track if the FAB should be extended based on scroll direction
    val isFabExtended =
        remember {
            derivedStateOf {
                // Show extended FAB when at the top of the list or scrolling up
                gridState.firstVisibleItemIndex == 0 || !gridState.canScrollBackward
            }
        }

    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showResetProgressDialog by rememberSaveable { mutableStateOf(false) }
    var showSortSheet by rememberSaveable { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(habitToShowcase) {
        gridState.animateScrollToItem(0)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it.asString(context),
                    withDismissAction = true,
                    duration = SnackbarDuration.Short,
                )
                clearErrorMessage()
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (habits.isNotEmpty()) {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    windowInsets = WindowInsets(0.dp),
                    title = {
                        Text(
                            text = "${getTimeOfDayGreeting()}, $username!",
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddHabit,
                expanded = isFabExtended.value,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add_a_habit),
                    )
                },
                text = {
                    Text(text = stringResource(R.string.add_a_habit))
                },
            )
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
                        val habit = habitToShowcase ?: return@item

                        HabitsScreenHeader(
                            quote = quote,
                            isResetProgressButtonLocked = isResetProgressButtonLocked,
                            isCensored = isCensoringHabitNames,
                            habitWithStreak = habit,
                            onEditHabit = { showEditDialog = true },
                            onDeleteHabit = { showDeleteDialog = true },
                            onViewLogs = { onViewLogs(habit.habit) },
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

                    if (habits.size == 1) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyDataScreen()
                        }
                    } else {
                        items(
                            habits,
                            key = { key -> key.habit.habitId },
                        ) { habitWithStreak ->
                            if (habitWithStreak.habit.habitId != habitToShowcase?.habit?.habitId) {
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
        if (showEditDialog && !isCensoringHabitNames) {
            EditHabitDialog(
                initialHabitName = habitToShowcase.habit.name,
                onDismissRequest = { showEditDialog = false },
                onSaveClick = { updatedHabit ->
                    onEditHabit(habitToShowcase.habit.copy(name = updatedHabit))
                    showEditDialog = false
                },
            )
        } else if (showEditDialog && isCensoringHabitNames) {
            LaunchedEffect(true) {
                scope.launch {
                    showEditDialog = false
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.cannot_edit_censored_habit_names),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
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
                errorMessage = null,
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
                onAddHabit = {},
                clearErrorMessage = {},
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
