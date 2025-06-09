package io.track.habit.ui.utils

import android.annotation.SuppressLint
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.ResourceResolutionException
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource

/**
 * A reflective way of loading a drawable resource to a painter.
 *
 * @param name The name of the resource.
 * @param packageName The package name of the resource.
 *
 * @return A painter for the resource.
 * @throws Exception If the resource is not found.
 * */
@SuppressLint("DiscouragedApi")
@Composable
fun painterResourceFromString(
    name: String,
    packageName: String = LocalContext.current.packageName,
): Painter {
    val res = LocalResources.current
    val context = LocalContext.current

    val id = res.getIdentifier(name, "drawable", packageName)
    if (id == 0) {
        throw Exception("Resource with name '$name' not found in package '$packageName'.")
    }

    val value = remember { TypedValue() }
    res.getValue(id, value, true)
    val path = value.string

    return if (path?.endsWith(".xml") == true) {
        val imageVector =
            remember(id, res, res.configuration) {
                ImageVector.vectorResource(null, res, id)
            }

        rememberVectorPainter(imageVector)
    } else {
        // Otherwise load the bitmap resource
        val imageBitmap =
            remember(path, id, context.theme) {
                try {
                    ImageBitmap.imageResource(res, id)
                } catch (exception: Exception) {
                    throw ResourceResolutionException("Error attempting to load resource: $path", exception)
                }
            }

        BitmapPainter(imageBitmap)
    }
}
