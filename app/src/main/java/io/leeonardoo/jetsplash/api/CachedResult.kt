package io.leeonardoo.jetsplash.api

sealed class CachedResult<out T, out E>(open val data: T?) {

    data class Loading<out T>(override val data: T?) : CachedResult<T, Nothing>(data)

    data class Success<out T>(override val data: T) : CachedResult<T, Nothing>(data)

    data class Error<out T, out E>(override val data: T?, val errorResult: E, val code: Int) :
        CachedResult<T, E>(data)

    data class NotFound<out T>(override val data: T?, val error: String) :
        CachedResult<T, Nothing>(data)

    data class NetworkError<out T>(override val data: T?, val error: String) :
        CachedResult<T, Nothing>(data)

    data class GenericError<out T>(override val data: T?, val error: String, val code: Int) :
        CachedResult<T, Nothing>(data)

    /**
     * Returns if the result is some sort of error (first value)
     * and the error String (second value)
     */
    fun getStatus(fromErrorResult: (E) -> String): Pair<Boolean, String?> {
        return when (this) {
            is Loading -> false to null

            is Success -> false to null

            is Error -> true to fromErrorResult(errorResult)

            is NotFound -> true to error

            is NetworkError<*> -> true to error

            is GenericError<*> -> true to error
        }
    }
}