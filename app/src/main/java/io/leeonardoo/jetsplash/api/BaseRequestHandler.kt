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

open class BaseRequestHandler {

    protected open val moshi: Moshi = Moshi.Builder().build()

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