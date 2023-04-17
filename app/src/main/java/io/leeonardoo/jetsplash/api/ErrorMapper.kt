package io.leeonardoo.jetsplash.api

/**
 * A interface to map the error body to a String that can be displayed to the user.
 */
interface ErrorMapper {
    fun mapError(): String?
}