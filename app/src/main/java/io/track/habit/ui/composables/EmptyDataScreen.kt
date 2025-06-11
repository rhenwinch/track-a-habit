package io.track.habit.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.track.habit.R
import io.track.habit.ui.utils.UiConstants

@Composable
fun EmptyDataScreen(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.empty_title),
    message: String = stringResource(R.string.empty_habits_message),
    icon: Painter = painterResource(R.drawable.grin_with_sweat_emoji),
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .padding(UiConstants.ScreenPaddingHorizontal)
                .heightIn(400.dp)
                .fillMaxSize(),
    ) {
        Icon(
            painter = icon,
            contentDescription = stringResource(R.string.empty_data_icon_content_desc),
            modifier = Modifier.size(100.dp),
            tint = Color.Unspecified,
        )

        Text(
            text = title,
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
            text = message,
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
