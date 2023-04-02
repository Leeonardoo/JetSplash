package io.leeonardoo.jetsplash.api

import android.content.Context
import androidx.annotation.StringRes
import com.squareup.moshi.Moshi
import io.leeonardoo.jetsplash.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okio.IOException
import retrofit2.HttpException

/**
 * A Basic RequestHandler implementation that takes care of making the api call
 * inside a coroutine, then converting it into a NetworkResult with the specified
 * error model and emitting it into a new Flow.
 *
 * You're probably looking for this one instead of the old [BaseApiHandler]
 *
 * You should *ALWAYS* pass a [android.app.Application] Context if you are using it as a
 * singleton (which is probably the case) - otherwise you will have a memory leak
 */
open class BaseRequestHandler(private val appContext: Context) {

    protected val moshi: Moshi = Moshi.Builder().add(EnvelopeFactory.INSTANCE).build()

    /**
     * The function responsible for making the request and treating the possible errors, but
     * doesn't return a Flow
     *
     * @param errorClass The expected API error model
     * @param apiCall the API call it will try to execute
     * @param notFoundErrorMsg the StringRes of a 404 message
     * @param unknownErrorMsg the StringRes of a unknown/unexpected error
     * @param networkErrorMsg the StringRes of a network error
     */
    open suspend fun <T, E> handleAsResult(
        errorClass: Class<E>,
        @StringRes notFoundErrorMsg: Int = R.string.request_not_found,
        @StringRes unknownErrorMsg: Int = R.string.request_unknown_error,
        @StringRes networkErrorMsg: Int = R.string.request_network_error,
        apiCall: suspend () -> T
    ): NetworkResult<T, E> {
        return try {
            NetworkResult.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            onErrorResponse(
                errorClass = errorClass,
                notFoundErrorMsg = notFoundErrorMsg,
                unknownErrorMsg = unknownErrorMsg,
                networkErrorMsg = networkErrorMsg,
                error = throwable
            )
        }
    }

    /**
     * The function responsible for making a network request and treating the possible errors.
     * Always returns the data from [fetchFromLocal] regardless of the network request state.
     * It also maps every state from [NetworkResult] to [CachedResult].
     *
     * @param errorClass The expected API error model
     * @param fetchFromLocal the function that'll fetch the data from a local database
     * @param shouldFetchFromRemote whether or not the network call should be run
     * @param remoteCall the network call it will try to execute
     * @param saveRemoteData the function that'll save the new data to a local database
     * @param notFoundErrorMsg the StringRes of a 404 message
     * @param unknownErrorMsg the StringRes of a unknown/unexpected error
     * @param networkErrorMsg the StringRes of a network error
     */
    open suspend fun <DB, MODEL, ERROR> handleWithCache(
        errorClass: Class<ERROR>,
        fetchFromLocal: () -> Flow<DB>,
        shouldFetchFromRemote: (DB?) -> Boolean = { true },
        remoteCall: suspend () -> MODEL,
        saveRemoteData: suspend (MODEL) -> Unit = { },
        @StringRes notFoundErrorMsg: Int = R.string.request_not_found,
        @StringRes unknownErrorMsg: Int = R.string.request_unknown_error,
        @StringRes networkErrorMsg: Int = R.string.request_network_error
    ) = flow {
        emit(CachedResult.Loading(null))
        val localData = fetchFromLocal().firstOrNull()

        if (shouldFetchFromRemote(localData)) {
            emit(CachedResult.Loading(localData))

            val response = handleAsResult(
                errorClass,
                notFoundErrorMsg,
                unknownErrorMsg,
                networkErrorMsg
            ) {
                remoteCall()
            }

            when (response) {
                is NetworkResult.Success -> {
                    response.result?.let { saveRemoteData(it) }
                    emitAll(fetchFromLocal().map { dbData ->
                        CachedResult.Success(dbData)
                    })
                }

                is NetworkResult.NetworkError -> emitAll(fetchFromLocal().map {
                    CachedResult.NetworkError(it, response.error)
                })

                is NetworkResult.GenericError -> emitAll(fetchFromLocal().map {
                    CachedResult.GenericError(it, response.error, response.code)
                })

                is NetworkResult.NotFound -> emitAll(fetchFromLocal().map {
                    CachedResult.NotFound(it, response.error)
                })

                is NetworkResult.Error -> emitAll(fetchFromLocal().map {
                    CachedResult.Error(it, response.errorResult, response.code)
                })

                else -> {
                }
            }
        } else {
            emitAll(fetchFromLocal().map {
                CachedResult.Success(it)
            })
        }
    }.flowOn(Dispatchers.IO)

    open fun <T, E> onErrorResponse(
        errorClass: Class<E>,
        @StringRes notFoundErrorMsg: Int,
        @StringRes unknownErrorMsg: Int,
        @StringRes networkErrorMsg: Int,
        error: Throwable
    ): NetworkResult<T, E> {
        return when (error) {
            is IOException ->
                NetworkResult.NetworkError(appContext.getString(networkErrorMsg))

            is HttpException -> {
                val code = error.code()

                if (code == 404) {
                    NetworkResult.NotFound(appContext.getString(notFoundErrorMsg))
                } else {
                    val errorResponse = convertErrorBody(error, errorClass)
                    if (errorResponse != null) {
                        NetworkResult.Error(errorResponse, code)
                    } else {
                        //There was an error trying to deserialize the errorBody
                        NetworkResult.GenericError(appContext.getString(unknownErrorMsg), code)
                    }
                }
            }

            else -> NetworkResult.GenericError(appContext.getString(unknownErrorMsg), 0)
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