package io.leeonardoo.jetsplash.api

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.NoEncryptionGmailerrorred
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import io.leeonardoo.jetsplash.R

/**
 * A sealed class representing the possible error states of a network request.
 * It contains the error code and the error body if it exists.
 *
 * @param E The type of the error body witch should implement [ErrorMapper]
 * @param code The error code or -1 if it doesn't exist
 */
sealed class NetworkError<out E : ErrorMapper>(open val code: Int) {

    /**
     * A common description representing each state
     */
    val description: Int
        get() {
            return when (this) {
                is ErrorBody ->
                    R.string.request_unknown_error

                is Network ->
                    R.string.request_network_error

                is NotFound ->
                    R.string.request_not_found

                is ResponseSerialization ->
                    R.string.request_serialization_error

                is ServerError ->
                    R.string.request_server_error

                is Unknown ->
                    R.string.request_unknown_error

                is UntrustedConnection ->
                    R.string.request_network_untrusted
            }
        }

    /**
     * A common error icon representing each state
     */
    val imageVector: ImageVector
        get() {
            return when (this) {
                is ErrorBody ->
                    Icons.Outlined.ErrorOutline

                is Network ->
                    Icons.Outlined.CloudOff

                is NotFound ->
                    Icons.Outlined.LinkOff

                is ResponseSerialization ->
                    Icons.Outlined.ErrorOutline

                is ServerError ->
                    Icons.Outlined.ErrorOutline

                is Unknown ->
                    Icons.Outlined.ErrorOutline

                is UntrustedConnection ->
                    Icons.Outlined.NoEncryptionGmailerrorred
            }
        }

    /**
     * A common error drawable representing each state
     */
    @get:DrawableRes
    val iconRes: Int
        get() {
            return when (this) {
                is ErrorBody ->
                    R.drawable.outline_error_outline_72

                is Network ->
                    R.drawable.outline_cloud_off_72

                is NotFound ->
                    R.drawable.outline_link_off_72

                is ResponseSerialization ->
                    R.drawable.outline_error_outline_72

                is ServerError ->
                    R.drawable.outline_error_outline_72

                is Unknown ->
                    R.drawable.outline_error_outline_72

                is UntrustedConnection ->
                    R.drawable.outline_no_encryption_gmailerrorred_72
            }
        }

    //Server-side
    data class ErrorBody<out E : ErrorMapper>(
        override val code: Int,
        val error: E
    ) : NetworkError<E>(code = code)

    //Server-side
    data class ServerError(
        override val code: Int,
        val exception: Exception?
    ) : NetworkError<Nothing>(code)

    //Client-side
    data class Network(
        val exception: Exception?
    ) : NetworkError<Nothing>(code = -1)

    //Server-side
    data class NotFound(
        val exception: Exception?
    ) : NetworkError<Nothing>(code = 404)

    //Client-side
    data class ResponseSerialization(
        val exception: Exception?
    ) : NetworkError<Nothing>(code = -1)

    //Client-side
    data class UntrustedConnection(
        val exception: Exception?
    ) : NetworkError<Nothing>(code = -1)

    //???
    data class Unknown(
        override val code: Int = -1,
        val exception: Exception?
    ) : NetworkError<Nothing>(code = code)
}

/**
 * Returns a description of the error from a composable function
 */
@Composable
fun <E : ErrorMapper> NetworkError<E>.rememberErrorDescription(): String {
    val context = LocalContext.current
    return remember(this) {
        getErrorDescription(context)
    }
}

/**
 * Returns a description of the error
 */
fun <E : ErrorMapper> NetworkError<E>.getErrorDescription(context: Context): String {
    return if (this is NetworkError.ErrorBody) {
        //If the error has a custom description, use it. Otherwise, fallback to default description
        this.error.mapError() ?: context.getString(description)
    } else {
        context.getString(description)
    }
}
