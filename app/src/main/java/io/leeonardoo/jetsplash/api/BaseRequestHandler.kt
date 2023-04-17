package io.leeonardoo.jetsplash.api

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import okio.IOException
import retrofit2.HttpException
import javax.net.ssl.SSLException
import javax.security.cert.CertificateExpiredException

/**
 * A base class that handles network requests.
 * It provides a [handle] method that returns a [NetworkResult] and
 * a [handleAsFlow] method that returns a [Flow] of [NetworkResult].
 * It also provides a [handleWithCache] method that returns a [Flow] of [CachedResult] which
 * can be used to cache network requests a database or memory cache.
 * The [handle] and [handleAsFlow] methods are suspend functions and should be called from a
 * coroutine scope.
 * We also make sure the methods are always executed on the IO dispatcher.
 */
open class BaseRequestHandler {

    protected open val moshi: Moshi = Moshi.Builder().build()

    /**
     * Executes an suspending API call and handles any exceptions that may occur.
     * Returns a [NetworkResult] object containing the result of the API call or
     * an error wrapped in the [NetworkError] which can have an error body that implements
     * [ErrorMapper].
     *
     * @param errorClass an [ErrorMapper] to use for mapping known API errors (i.e. validations)
     * @param apiCall the API call to execute
     * @return a [NetworkResult] containing the result of the API call or an error
     */
    open suspend fun <T, E : ErrorMapper> handle(
        errorClass: Class<E>,
        apiCall: suspend () -> T
    ): NetworkResult<T, E> {
        return withContext(Dispatchers.IO) {
            try {
                NetworkResult.Success(apiCall.invoke())
            } catch (e: java.lang.Exception) {
                e.printStackTrace()

                NetworkResult.Error(onErrorResponse(errorClass = errorClass, error = e))
            }
        }
    }

    /**
     * Executes an suspending API call and handles any exceptions that may occur.
     * Returns a Flow of [NetworkResult] object containing the result of the API call or
     * an error wrapped in the [NetworkError] which can have an error body that implements
     * [ErrorMapper].
     *
     * @param errorClass an [ErrorMapper] to use for mapping known API errors (i.e. validations)
     * @param apiCall the API call to execute
     * @return a [Flow] containing of a [NetworkResult] that contains the result
     * of the API call or an error
     */
    open suspend fun <T, E : ErrorMapper> handleAsFlow(
        errorClass: Class<E>,
        apiCall: suspend () -> T
    ): Flow<NetworkResult<T, E>> {
        return flow {
            emit(
                handle(errorClass = errorClass, apiCall = apiCall)
            )
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Executes an API call that can be cached in a local database or memory.
     * If the local data is up to date (determined by [shouldFetchFromRemote]),
     * the function returns the cached data without making the API call.
     *
     * This function returns a [Flow] of [CachedResult] objects that represent the current
     * state of the data. It's possible that there is some data in every state if it's cached.
     *
     * @param errorClass an [ErrorMapper] to use for mapping known API errors (i.e. validations)
     * @param fetchFromLocal a function that returns a [Flow] of the cached data from the local database
     * @param shouldFetchFromRemote a function that takes the cached data as a parameter and
     * returns a boolean indicating whether the data should be fetched from the remote API
     * @param remoteCall the API call to execute if the data needs to be fetched from the remote API
     * @param saveRemoteData a function that should save the result of the API to the local database
     * @return a [Flow] of [CachedResult] objects that represent the current state of the data.
     */
    open suspend fun <DB, Model, Error : ErrorMapper> handleWithCache(
        errorClass: Class<Error>,
        fetchFromLocal: () -> Flow<DB>,
        shouldFetchFromRemote: (DB?) -> Boolean = { true },
        remoteCall: suspend () -> Model,
        saveRemoteData: suspend (Model) -> Unit = { }
    ) = flow {
        emit(CachedResult.Loading(null))
        val localData = fetchFromLocal().firstOrNull()

        if (shouldFetchFromRemote(localData)) {
            emit(CachedResult.Loading(localData))

            val response = handle(
                errorClass = errorClass,
                apiCall = remoteCall
            )

            when (response) {
                is NetworkResult.Success -> {
                    response.result?.let { saveRemoteData(it) }
                    emitAll(fetchFromLocal().map { dbData ->
                        CachedResult.Success(dbData)
                    })
                }

                is NetworkResult.Error -> emitAll(fetchFromLocal().map {
                    CachedResult.Error(it, response.error)
                })
            }
        } else {
            emitAll(fetchFromLocal().map {
                CachedResult.Success(it)
            })
        }
    }.flowOn(Dispatchers.IO)

    protected open fun <E : ErrorMapper> onErrorResponse(
        errorClass: Class<E>,
        error: Exception
    ): NetworkError<E> {
        return when (error) {
            is SSLException, is CertificateExpiredException -> {
                NetworkError.UntrustedConnection(error)
            }

            is IOException ->
                NetworkError.Network(error)

            is HttpException -> {
                val code = error.code()

                if (code == 404) {
                    NetworkError.NotFound(error)
                } else if (code == 500) {
                    NetworkError.ServerError(code, error)
                } else {
                    val errorResponse = convertErrorBody(error, errorClass)
                    if (errorResponse != null) {
                        NetworkError.ErrorBody(code, errorResponse)
                    } else {
                        //There was an error trying to deserialize the errorBody
                        NetworkError.Unknown(-1, error)
                    }
                }
            }

            is NullPointerException -> {
                NetworkError.ResponseSerialization(error)
            }

            is JsonDataException -> {
                NetworkError.ResponseSerialization(error)
            }

            else -> NetworkError.Unknown(-1, error)
        }
    }

    open fun <E> convertErrorBody(throwable: HttpException, errorClass: Class<E>): E? {
        return try {
            throwable.response()?.errorBody()?.source()?.let {
                moshi.adapter(errorClass).fromJson(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}