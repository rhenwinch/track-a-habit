package io.track.habit.ui.screens.streaks.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.track.habit.R
import io.track.habit.ui.theme.TrackAHabitTheme

@Composable
fun StatsLabel(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start,
        modifier = modifier,
    ) {
        Text(
            text = title,
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Preview
@Composable
private fun StatsLabelPreview() {
    TrackAHabitTheme {
        Surface {
            StatsLabel(
                title = stringResource(R.string.longest_streaks),
                description = stringResource(R.string.longest_streaks_desc),
            )
        }
    }
}
