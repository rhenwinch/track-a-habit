package io.track.habit.ui.screens.habits.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.track.habit.R
import io.track.habit.ui.theme.TrackAHabitTheme

@Composable
fun AddHabitFab(
    onClick: () -> Unit,
    isExtended: Boolean,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        expanded = isExtended,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.add_icon_content_desc),
            )
        },
        text = {
            AnimatedVisibility(
                visible = isExtended,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
            ) {
                Text(text = stringResource(R.string.add_a_habit))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun AddHabitFabPreview() {
    TrackAHabitTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Row {
                AddHabitFab(isExtended = true, onClick = {})
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddHabitFabCollapsedPreview() {
    TrackAHabitTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Row {
                AddHabitFab(isExtended = false, onClick = {})
            }
        }
    }
}
