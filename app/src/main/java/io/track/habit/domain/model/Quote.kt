package io.track.habit.domain.model

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
