package io.track.habit.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun GradientFireIcon(
    painter: Painter,
    contentDescription: String?,
    gradient: Brush,
    modifier: Modifier = Modifier,
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
