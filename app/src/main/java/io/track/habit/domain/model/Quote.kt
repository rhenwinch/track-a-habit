package io.track.habit.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Quote(
    val message: String,
    val author: String,
) {
    override fun toString(): String {
        return """
            $message

            — $author
            """.trimIndent()
    }
}
