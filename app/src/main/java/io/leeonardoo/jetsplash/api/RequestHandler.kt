package io.leeonardoo.jetsplash.api

import android.content.Context
import androidx.annotation.StringRes
import com.squareup.moshi.JsonDataException
import okio.IOException
import retrofit2.HttpException
import timber.log.Timber

class RequestHandler(
    private val appContext: Context
) : BaseRequestHandler(appContext) {

    /**
     * It's overridden as all errors returned by our API are wrapped inside a "errors" field
     */
    override fun <E> convertErrorBody(throwable: HttpException, errorClass: Class<E>): E? {
        return try {
            throwable.response()?.errorBody()?.source()?.let {
                moshi.adapter<E?>(errorClass, Enveloped::class.java).fromJson(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun <T, E> onErrorResponse(
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
                } else if (code == 500) {
                    Timber.d(error, "Internal server error. This is probably an API bug.")
                    NetworkResult.GenericError(appContext.getString(unknownErrorMsg), code)
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

            is NullPointerException -> {
                Timber.e(
                    error,
                    "Probably the local object is incorrect or the server sent a unexpected response."
                )
                NetworkResult.GenericError(appContext.getString(unknownErrorMsg), 0)
            }

            is JsonDataException -> {
                Timber.e(
                    error,
                    "An error occurred while converting the JSON object. Probably the object is incorrect or the server sent a unexpected response."
                )
                NetworkResult.GenericError(appContext.getString(unknownErrorMsg), 0)
            }

            else -> NetworkResult.GenericError(appContext.getString(unknownErrorMsg), 0)
        }
    }
}