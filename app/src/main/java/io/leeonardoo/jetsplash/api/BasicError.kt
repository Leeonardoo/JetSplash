package io.leeonardoo.jetsplash.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BasicError(

    @Json(name = "errors")
    val errors: List<String>

) : ErrorMapper {

    override fun mapError(): String? = errors.joinToString().ifBlank { null }
}