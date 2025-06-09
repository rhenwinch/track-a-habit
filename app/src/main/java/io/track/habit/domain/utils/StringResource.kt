package io.track.habit.domain.utils

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

/**
 * Sealed class that can represent either a string resource or a literal string
 */
sealed class StringResource {
    /**
     * Represents a string value.
     * @param value The string value.
     */
    data class Literal(
        val value: String,
    ) : StringResource() {
        constructor(e: Throwable?) : this(e?.message ?: "")
    }

    /**
     * Represents a string resource.
     * @param id The string resource ID.
     * @param args Optional arguments to format the string resource.
     */
    class Resource(
        @StringRes val id: Int,
        vararg val args: Any,
    ) : StringResource()

    /**
     * Represents a plural string resource.
     * @param id The plural string resource ID.
     * @param quantity The quantity to determine which plural string to use.
     * @param args Optional formatting arguments for the selected string.
     */
    class Plural(
        @PluralsRes val id: Int,
        val quantity: Int,
        vararg val args: Any,
    ) : StringResource()

    /**
     * Returns the text as a string.
     * @param context The context used to retrieve string resources.
     * @return The text as a string.
     */
    fun asString(context: Context): String {
        return when (this) {
            is Literal -> value
            is Resource -> context.getString(id, *args)
            is Plural -> context.resources.getQuantityString(id, quantity, *args)
        }
    }

    /**
     * Returns the text as a string for Composable functions.
     * @return The text as a string.
     */
    @Composable
    @ReadOnlyComposable
    fun asString(): String {
        return when (this) {
            is Literal -> value
            is Resource -> stringResource(id = id, *args)
            is Plural -> pluralStringResource(id = id, count = quantity, *args)
        }
    }
}

fun stringRes(
    @StringRes resId: Int,
    vararg args: Any,
): StringResource = StringResource.Resource(resId, *args)

fun stringLiteral(value: String): StringResource = StringResource.Literal(value)

fun stringLiteral(error: Throwable?): StringResource = StringResource.Literal(error)

/**
 * Creates a StringResource that represents a plural string resource.
 * @param resId The plural resource ID.
 * @param quantity The quantity to determine which plural string to use.
 * @param args Optional formatting arguments for the selected string.
 * @return A StringResource wrapping the plural string resource.
 */
fun pluralStringRes(
    @PluralsRes resId: Int,
    quantity: Int,
    vararg args: Any,
): StringResource = StringResource.Plural(resId, quantity, *args)
