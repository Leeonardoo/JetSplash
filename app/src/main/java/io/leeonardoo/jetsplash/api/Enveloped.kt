package io.leeonardoo.jetsplash.api

import com.squareup.moshi.*
import java.lang.reflect.Type

/**
 * Indicates an endpoint wraps a response in a JSON Object.
 * When deserializing the response we should only return
 * what's inside the outer most object.
 */
//From the article https://medium.com/@naturalwarren/moshi-made-simple-jsonqualifier-b99559c826ad
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class Enveloped

/**
 * Complements @Enveloped by performing custom deserialization
 * for a response that is wrapped in a JSON Object.
 */
class EnvelopeFactory : JsonAdapter.Factory {
    companion object {
        val INSTANCE = EnvelopeFactory()
    }

    override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        val delegateAnnotations =
            Types.nextAnnotations(annotations, Enveloped::class.java) ?: return null

        val delegate = moshi.nextAdapter<Any>(this, type, delegateAnnotations)

        return EnvelopeJsonAdapter(delegate)
    }

    private class EnvelopeJsonAdapter(val delegate: JsonAdapter<*>) : JsonAdapter<Any>() {
        override fun fromJson(reader: JsonReader): Any? {
            reader.beginObject()
            reader.skipName()
            val envelope = delegate.fromJson(reader)
            reader.endObject()
            return envelope
        }

        override fun toJson(writer: JsonWriter, value: Any?) =
            throw UnsupportedOperationException("@Enveloped is only used to deserialize objects.")
    }
}