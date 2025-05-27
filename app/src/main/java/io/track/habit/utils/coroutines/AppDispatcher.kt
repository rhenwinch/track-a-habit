package io.track.habit.utils.coroutines

import io.track.habit.utils.coroutines.AppDispatcher.Companion.launchOnDefault
import io.track.habit.utils.coroutines.AppDispatcher.Companion.launchOnIO
import io.track.habit.utils.coroutines.AppDispatcher.Companion.launchOnMain
import io.track.habit.utils.coroutines.AppDispatcher.Companion.runOnDefault
import io.track.habit.utils.coroutines.AppDispatcher.Companion.runOnIO
import io.track.habit.utils.coroutines.AppDispatcher.Companion.runOnMain
import io.track.habit.utils.coroutines.AppDispatcher.Companion.withDefaultContext
import io.track.habit.utils.coroutines.AppDispatcher.Companion.withIOContext
import io.track.habit.utils.coroutines.AppDispatcher.Companion.withMainContext
import io.track.habit.utils.coroutines.AppDispatcher.Default
import io.track.habit.utils.coroutines.AppDispatcher.IO
import io.track.habit.utils.coroutines.AppDispatcher.Main
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Enum class representing different coroutine dispatchers for the application.
 * Each enum constant encapsulates a [CoroutineDispatcher] and a corresponding [CoroutineScope].
 *
 * @property dispatcher The underlying [CoroutineDispatcher] for this dispatcher type.
 * @property scope A [CoroutineScope] associated with this dispatcher, using a [SupervisorJob].
 *
 * @see Default
 * @see IO
 * @see Main
 */
enum class AppDispatcher(
    val dispatcher: CoroutineDispatcher,
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
) {
    /**
     * Represents the default dispatcher, suitable for CPU-intensive tasks.
     *
     * @see withDefaultContext
     * @see launchOnDefault
     * @see runOnDefault
     */
    Default(Dispatchers.Default),

    /**
     * Represents the IO dispatcher, optimized for disk and network operations.
     *
     * @see withIOContext
     * @see launchOnIO
     * @see runOnIO
     */
    IO(Dispatchers.IO),

    /**
     * Represents the Main dispatcher, used for UI-related operations.
     *
     * @see withMainContext
     * @see launchOnMain
     * @see runOnMain
     */
    Main(Dispatchers.Main),
    ;

    /**
     * The companion object provides utility functions for working with coroutines
     * */
    companion object {
        /**
         * This uses withContext([Dispatchers.IO]) for suspend functions.
         *
         * @param block The suspend function to run.
         *
         * @see runOnIO
         * @see launchOnIO
         * */
        suspend inline fun <T> withIOContext(crossinline block: suspend CoroutineScope.() -> T): T =
            withContext(IO.dispatcher) {
                block()
            }

        /**
         * This uses `withContext([Dispatchers.Default])` for suspend functions.
         *
         * @param block The suspend function to run.
         *
         * @see runOnDefault
         * @see launchOnDefault
         * */
        suspend inline fun <T> withDefaultContext(crossinline block: suspend CoroutineScope.() -> T): T =
            withContext(Default.dispatcher) {
                block()
            }

        /**
         * This uses `withContext([Dispatchers.Main])` for suspend functions.
         *
         * @param block The suspend function to run.
         *
         * @see runOnMain
         * @see launchOnMain
         * */
        suspend inline fun <T> withMainContext(crossinline block: suspend CoroutineScope.() -> T): T =
            withContext(Main.dispatcher) {
                block()
            }

        /**
         * This uses `runBlocking([Dispatchers.IO])` for **non** suspend functions.
         *
         * @param block The suspend function to run.
         *
         * @see withIOContext
         * @see launchOnIO
         * */
        inline fun <T> runOnIO(crossinline block: suspend CoroutineScope.() -> T): T =
            runBlocking(IO.dispatcher) {
                block()
            }

        /**
         * This uses `runBlocking([Dispatchers.Default])` for **non** suspend functions.
         *
         * @param block The suspend function to run.
         *
         * @see withDefaultContext
         * @see launchOnDefault
         * */
        inline fun <T> runOnDefault(crossinline block: suspend CoroutineScope.() -> T): T =
            runBlocking(Default.dispatcher) {
                block()
            }

        /**
         * This uses `runBlocking([Dispatchers.Main])` for **non** suspend functions.
         *
         * @param block The suspend function to run.
         *
         * @see withMainContext
         * @see launchOnMain
         * */
        inline fun <T> runOnMain(crossinline block: suspend CoroutineScope.() -> T): T =
            runBlocking(Main.dispatcher) {
                block()
            }

        /**
         * Launches a new coroutine on the [Dispatchers.IO] context and executes the given block.
         *
         * @param block The suspend function to run.
         *
         * @see withIOContext
         * @see runOnIO
         */
        inline fun launchOnIO(crossinline block: suspend CoroutineScope.() -> Unit) {
            IO.scope.launch {
                block()
            }
        }

        /**
         * Launches a new coroutine on the [Dispatchers.Default] context and executes the given block.
         *
         * @param block The suspend function to run.
         *
         * @see withDefaultContext
         * @see runOnDefault
         */
        inline fun launchOnDefault(crossinline block: suspend CoroutineScope.() -> Unit) {
            Default.scope.launch {
                block()
            }
        }

        /**
         * Launches a new coroutine on the [Dispatchers.Main] context and executes the given block.
         *
         * @param block The suspend function to run.
         *
         * @see withMainContext
         * @see runOnMain
         */
        inline fun launchOnMain(crossinline block: suspend CoroutineScope.() -> Unit) {
            Main.scope.launch {
                block()
            }
        }
    }
}
