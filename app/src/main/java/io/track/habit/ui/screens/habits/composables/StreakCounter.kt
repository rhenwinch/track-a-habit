package io.track.habit.ui.screens.habits.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.track.habit.R
import io.track.habit.ui.theme.TrackAHabitTheme
import io.track.habit.ui.utils.FireGradientGenerator
import kotlinx.coroutines.delay

@Composable
fun StreakCounter(streak: Int) {
    val streakGradient = FireGradientGenerator.getGradient(streak)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GradientIcon(
            painter = painterResource(R.drawable.streak_filled),
            contentDescription = stringResource(R.string.streak_icon_content_desc),
            modifier =
                Modifier
                    .width(22.dp)
                    .height(27.dp),
            gradient = streakGradient,
        )

        GradientText(
            text = streak.toString(),
            gradient = streakGradient,
            style =
                LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 2f
                    )
                ),
        )
    }
}

@Composable
private fun GradientIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    gradient: Brush,
) {
    Box(modifier = modifier) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            tint = Color.Transparent,
        )

        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.99F)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = gradient,
                            blendMode = BlendMode.SrcAtop,
                        )
                    },
            tint = Color.White,
        )
    }
}

@Composable
private fun GradientText(
    text: String,
    gradient: Brush,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = style,
        modifier =
            modifier
                .graphicsLayer(alpha = 0.99F)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = gradient,
                        blendMode = BlendMode.SrcAtop,
                    )
                },
    )
}

@Preview
@Composable
private fun StreakCounterPreview() {
    var streakDays by remember { mutableIntStateOf(0) }

    LaunchedEffect(true) {
        while (streakDays < 1200) {
            streakDays++
            delay(10)
        }
    }

    TrackAHabitTheme {
        Surface {
            StreakCounter(streakDays)
        }
    }
}
