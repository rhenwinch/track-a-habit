package io.track.habit.domain.utils

sealed class SortOrder(
    val ascending: Boolean,
) {
    class Name(
        ascending: Boolean = true,
    ) : SortOrder(ascending)

    class Creation(
        ascending: Boolean = true,
    ) : SortOrder(ascending)

    class Streak(
        ascending: Boolean = true,
    ) : SortOrder(ascending)
}
