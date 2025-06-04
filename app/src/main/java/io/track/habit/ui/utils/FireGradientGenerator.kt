package io.track.habit.ui.utils

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/**
 * Utility object for generating fire-like gradients.
 *
 * This object provides a method to create dynamic gradients that visually represent
 * the intensity of a streak, typically used for habit tracking or gamification.
 * The gradient transitions through a predefined spectrum of colors, simulating
 * the appearance of a flame growing hotter with increasing streak duration.
 *
 * The core logic involves:
 * 1. Defining a base set of colors representing different stages of "fire".
 * 2. Calculating an "intensity" value based on the number of streak days and predefined milestones.
 * 3. Generating variants of the base colors (lighter and darker shades) to create a more nuanced flame effect.
 * 4. Blending these colors based on the calculated intensity to produce the final gradient.
 *
 * Helper functions are used for:
 *  - Adjusting color properties (Hue, Saturation, Lightness).
 *  - Linearly interpolating between colors.
 */
object FireGradientGenerator {
    private val baseColors =
        listOf(
            Color(0xFF9CA3AF), // Gray
            Color(0xFF78350F), // Brown (ember)
            Color(0xFFFBBF24), // Yellow
            Color(0xFFF97316), // Orange
            Color(0xFFEF4444), // Red
            Color(0xFFEC4899), // Pink
            Color(0xFFA855F7), // Purple
            Color(0xFF8B5CF6), // Violet
            Color(0xFF6366F1), // Indigo
        )

    /**
     * Generates a fire-like gradient based on the number of streak days.
     * The gradient transitions through a spectrum of colors as the streak days increase,
     * simulating the intensity of a flame.
     *
     * @param streakDays The number of consecutive days for the streak.
     * @return A [Brush] object representing the calculated gradient.
     */
    fun getGradient(streakDays: Int): Brush {
        val milestones = listOf(0f, 30f, 150f, 365f, 730f, 1095f)
        val intensityLevels = listOf(0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f)

        val intensity = calculateIntensity(streakDays.toFloat(), milestones, intensityLevels)

        return createGradientFromIntensity(intensity)
    }

    private fun calculateIntensity(
        streakDays: Float,
        milestones: List<Float>,
        intensityLevels: List<Float>,
    ): Float {
        val clampedDays = streakDays.coerceAtMost(milestones.last())

        for (i in 0 until milestones.size - 1) {
            if (clampedDays <= milestones[i + 1]) {
                val lowerMilestone = milestones[i]
                val upperMilestone = milestones[i + 1]
                val lowerIntensity = intensityLevels[i]
                val upperIntensity = intensityLevels[i + 1]

                val progress = (clampedDays - lowerMilestone) / (upperMilestone - lowerMilestone)

                return lowerIntensity + (upperIntensity - lowerIntensity) * progress
            }
        }

        return intensityLevels.last()
    }

    private fun createGradientFromIntensity(intensity: Float): Brush {

        val (lowColors, highColors) = generateFireGradientVariants(baseColors)

        val primaryColor = blendColorsByIntensity(intensity, baseColors)
        val lowColor = blendColorsByIntensity(intensity, lowColors)
        val highColor = blendColorsByIntensity(intensity, highColors)

        return Brush.verticalGradient(
            colors = listOf(lowColor, primaryColor, highColor),
        )
    }

    private fun generateFireGradientVariants(
        baseColors: List<Color>,
        lowHueShift: Float = 5f,
        highHueShift: Float = -10f,
        lowSaturationDelta: Float = -0.1f,
        highSaturationDelta: Float = 0.1f,
        lowLightness: Float = 0.8f,
        highLightness: Float = 0.35f,
    ): Pair<List<Color>, List<Color>> {
        val lowColors =
            baseColors.map {
                it.adjustHSL(
                    hueShift = lowHueShift,
                    saturationDelta = lowSaturationDelta,
                    targetLightness = lowLightness,
                )
            }

        val highColors =
            baseColors.map {
                it.adjustHSL(
                    hueShift = highHueShift,
                    saturationDelta = highSaturationDelta,
                    targetLightness = highLightness,
                )
            }

        return Pair(lowColors, highColors)
    }

    private fun Color.adjustHSL(
        hueShift: Float = 0f,
        saturationDelta: Float = 0f,
        targetLightness: Float = 0.5f,
    ): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(this.toArgb(), hsl)

        // Hue: Wrap around [0, 360)
        hsl[0] =
            (hsl[0] + hueShift).let {
                if (it < 0) {
                    it + 360
                } else if (it >= 360) {
                    it - 360
                } else {
                    it
                }
            }

        // Saturation: Clamp between 0–1
        hsl[1] = (hsl[1] + saturationDelta).coerceIn(0f, 1f)

        // Lightness: Directly set and clamp
        hsl[2] = targetLightness.coerceIn(0f, 1f)

        return Color(ColorUtils.HSLToColor(hsl))
    }

    private fun blendColorsByIntensity(
        intensity: Float,
        colors: List<Color>,
    ): Color {
        val clampedIntensity = intensity.coerceIn(0f, 1f)
        val scaledIntensity = clampedIntensity * (colors.size - 1)
        val lowerIndex = scaledIntensity.toInt().coerceAtMost(colors.size - 2)
        val upperIndex = (lowerIndex + 1).coerceAtMost(colors.size - 1)
        val blendFactor = scaledIntensity - lowerIndex

        return lerp(colors[lowerIndex], colors[upperIndex], blendFactor)
    }

    private fun lerp(
        start: Color,
        end: Color,
        fraction: Float,
    ): Color {
        val clampedFraction = fraction.coerceIn(0f, 1f)
        return Color(
            red = start.red + (end.red - start.red) * clampedFraction,
            green = start.green + (end.green - start.green) * clampedFraction,
            blue = start.blue + (end.blue - start.blue) * clampedFraction,
            alpha = start.alpha + (end.alpha - start.alpha) * clampedFraction,
        )
    }
}
