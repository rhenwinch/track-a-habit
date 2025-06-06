package io.track.habit.ui.screens.habits.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.track.habit.R
import io.track.habit.domain.utils.SortOrder
import io.track.habit.ui.theme.TrackAHabitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterBottomSheet(
    currentSortOrder: SortOrder,
    onSortOrderSelect: (SortOrder) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.sort_by),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SortOption(
                    label = stringResource(R.string.sort_by_name),
                    isSelected = currentSortOrder is SortOrder.Name,
                    isAscending = (currentSortOrder as? SortOrder.Name)?.ascending ?: true,
                    onClick = {
                        if (currentSortOrder is SortOrder.Name) {
                            onSortOrderSelect(SortOrder.Name(!currentSortOrder.ascending))
                        } else {
                            onSortOrderSelect(SortOrder.Name(true))
                        }
                    },
                )

                SortOption(
                    label = stringResource(R.string.sort_by_creation_date),
                    isSelected = currentSortOrder is SortOrder.Creation,
                    isAscending = (currentSortOrder as? SortOrder.Creation)?.ascending ?: true,
                    onClick = {
                        if (currentSortOrder is SortOrder.Creation) {
                            onSortOrderSelect(SortOrder.Creation(!currentSortOrder.ascending))
                        } else {
                            onSortOrderSelect(SortOrder.Creation(true))
                        }
                    },
                )

                SortOption(
                    label = stringResource(R.string.sort_by_streak),
                    isSelected = currentSortOrder is SortOrder.Streak,
                    isAscending = (currentSortOrder as? SortOrder.Streak)?.ascending ?: true,
                    onClick = {
                        if (currentSortOrder is SortOrder.Streak) {
                            onSortOrderSelect(SortOrder.Streak(!currentSortOrder.ascending))
                        } else {
                            onSortOrderSelect(SortOrder.Streak(true))
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SortOption(
    label: String,
    isSelected: Boolean,
    isAscending: Boolean,
    onClick: () -> Unit,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isAscending) 0f else 180f,
        label = "arrow_rotation",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .padding(8.dp),
    ) {
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.arrow_upward),
                contentDescription =
                    if (isAscending) {
                        stringResource(R.string.ascending_icon_content_desc)
                    } else {
                        stringResource(R.string.descending_icon_content_desc)
                    },
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .size(16.dp)
                        .rotate(arrowRotation),
            )

            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun FilterBottomSheetPreview() {
    TrackAHabitTheme {
        var currentSortOrder: SortOrder by remember { mutableStateOf(SortOrder.Streak()) }

        FilterBottomSheet(
            currentSortOrder = currentSortOrder,
            onSortOrderSelect = { currentSortOrder = it },
            onDismiss = {},
            sheetState = rememberModalBottomSheetState(),
        )
    }
}
