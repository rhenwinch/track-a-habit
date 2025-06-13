package io.track.habit.ui.screens.onboarding.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import io.track.habit.ui.theme.TrackAHabitTheme

@Composable
fun CustomEllipse(
    modifier: Modifier = Modifier,
    shadowColor: Color = Color.Black.copy(alpha = 0.3f),
    shadowRadius: Float = 20f,
    shadowOffsetX: Float = 0f,
    shadowOffsetY: Float = 10f,
) {
    val primary = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier.fillMaxSize(),
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Circle parameters
        val radius = maxOf(size.width, size.height) * 0.45f
        val offsetY = -size.height * 0.33f
        val offsetX = size.width * 0.15f
        val circleCenter = Offset(centerX + offsetX, centerY + offsetY)

        // Draw shadow using Paint
        drawIntoCanvas { canvas ->
            val paint =
                Paint().apply {
                    color = shadowColor
                    isAntiAlias = true
                }

            // Create shadow effect using native paint
            paint.asFrameworkPaint().apply {
                setShadowLayer(
                    shadowRadius,
                    shadowOffsetX,
                    shadowOffsetY,
                    shadowColor.toArgb(),
                )
            }

            canvas.drawCircle(
                center = circleCenter,
                radius = radius,
                paint = paint,
            )
        }

        // Draw the main circle on top
        drawCircle(
            color = primary,
            radius = radius,
            center = circleCenter,
        )
    }
}

@Preview(name = "Default Shadow")
@Composable
private fun CustomEllipsePreview() {
    TrackAHabitTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
            ) {
                CustomEllipse()
            }
        }
    }
}
