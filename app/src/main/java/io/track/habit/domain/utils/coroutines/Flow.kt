package io.track.habit.domain.utils.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

fun <T> Flow<T>.awaitFirst() = runBlocking(context = AppDispatcher.IO.dispatcher) { this@awaitFirst.first() }

fun <T> Flow<T>.asStateFlow(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed(5000),
    initialValue: T = awaitFirst(),
) = stateIn(
    scope = scope,
    started = started,
    initialValue = initialValue,
)
