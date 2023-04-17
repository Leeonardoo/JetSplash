package io.leeonardoo.jetsplash.api

/**
 * A sealed class that represents the result of a network request.
 * The result can be either a [Success] or an [Error].
 *
 * The [Error] class contains a [NetworkError] which contains the error code
 * and the error body if it exists.
 *
 * @param T The type of the successful result
 * @param E The type of the error result
 */
sealed class NetworkResult<out T, out E : ErrorMapper> {

    data class Success<out T>(val result: T) : NetworkResult<T, Nothing>()

    data class Error<out E : ErrorMapper>(val error: NetworkError<E>) : NetworkResult<Nothing, E>()

}