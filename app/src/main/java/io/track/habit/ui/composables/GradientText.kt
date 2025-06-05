package io.track.habit.ui.composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle

@Composable
fun GradientText(
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
