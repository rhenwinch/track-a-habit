package io.track.habit.ui.utils

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource

/**
 * Sealed class that can represent either a drawable resource or an ImageVector
 */
sealed class DrawableResource {
    /**
     * Represents a drawable resource ID
     * @param resId The drawable resource ID
     */
    class Resource(
        @DrawableRes val resId: Int,
    ) : DrawableResource()

    /**
     * Represents an ImageVector (like Material Icons)
     * @param imageVector The ImageVector instance
     */
    data class Vector(
        val imageVector: ImageVector,
    ) : DrawableResource()

    @Composable
    fun asPainter(): Painter {
        return when (this) {
            is Resource -> painterResource(id = resId)
            is Vector -> rememberVectorPainter(imageVector)
        }
    }
}

fun drawableRes(
    @DrawableRes resId: Int,
): DrawableResource = DrawableResource.Resource(resId)

fun vectorRes(imageVector: ImageVector): DrawableResource = DrawableResource.Vector(imageVector)
