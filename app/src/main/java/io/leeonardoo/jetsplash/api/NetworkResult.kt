package io.leeonardoo.jetsplash.api

sealed class NetworkResult<out T, out E> {

    data class Success<out T>(val result: T) : NetworkResult<T, Nothing>()

    data class Error<out E>(val error: NetworkError<E>) : NetworkResult<Nothing, E>()

}