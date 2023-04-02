package io.leeonardoo.jetsplash.api

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.NoEncryptionGmailerrorred
import androidx.compose.ui.graphics.vector.ImageVector
import io.leeonardoo.jetsplash.R

sealed class NetworkError<out E>(open val code: Int) {

    /**
     * A common description representing each state
     */
    val description: String
        get() = ""

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
    data class ErrorBody<out E>(
        val error: E,
        override val code: Int
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
    object NotFound : NetworkError<Nothing>(code = 404)

    //Client-side
    data class ResponseSerialization(
        val exception: Exception?
    ) : NetworkError<Nothing>(code = -1)

    //Client-side
    data class UntrustedConnection(
        val exception: Exception?
    ): NetworkError<Nothing>(code = -1)

    //???
    data class Unknown(
        override val code: Int = -1,
        val exception: Exception?
    ) : NetworkError<Nothing>(code = code)

}