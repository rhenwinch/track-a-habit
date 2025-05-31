package io.track.habit.domain.utils

/**
 * Sealed class representing the state of a resource operation.
 * Commonly used in repositories, use cases, and other data layer components
 * to wrap the result of asynchronous operations.
 */
sealed class Resource<out T> {
    /**
     * Represents a loading state
     */
    object Loading : Resource<Nothing>()

    /**
     * Represents a successful state with data
     * @param data The successfully retrieved data
     */
    data class Success<T>(
        override val data: T,
    ) : Resource<T>()

    /**
     * Represents an error state
     * @param message Error message describing what went wrong
     * @param throwable Optional throwable for more detailed error information
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : Resource<Nothing>()

    fun isLoading(): Boolean = this is Loading

    fun isSuccess(): Boolean = this is Success

    fun isError(): Boolean = this is Error

    open val data: T?
        get() {
            return when (this) {
                is Success -> data
                else -> null
            }
        }

    val error: String?
        get() {
            return when (this) {
                is Error -> message
                else -> null
            }
        }
}
