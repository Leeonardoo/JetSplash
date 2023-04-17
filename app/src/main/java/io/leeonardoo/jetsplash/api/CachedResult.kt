package io.leeonardoo.jetsplash.api

/**
 * A sealed class that represents the result of a cached network request.
 * The result can be either a [Loading], [Success] or an [Error].
 * There can be cached data or not in the [Loading] and [Error] states.
 * The [Success] state always contains data.
 *
 * @param T The type of the successful result
 * @param E The type of the error result
 * @property data The cached or response data
 */
sealed class CachedResult<out T, out E : ErrorMapper>(open val data: T?) {

    data class Loading<out T>(override val data: T?) : CachedResult<T, Nothing>(data)

    data class Success<out T>(override val data: T) : CachedResult<T, Nothing>(data)

    data class Error<out T, out E : ErrorMapper>(
        override val data: T?,
        val error: NetworkError<E>
    ) : CachedResult<T, E>(data)

}