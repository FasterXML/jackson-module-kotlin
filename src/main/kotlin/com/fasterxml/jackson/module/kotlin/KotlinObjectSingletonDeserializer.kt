package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer

internal fun JsonDeserializer<*>.asSingletonDeserializer(singleton: Any) =
        KotlinObjectSingletonDeserializer(singleton, this)

/** deserialize as normal, but return the canonical singleton instance. */
internal class KotlinObjectSingletonDeserializer(
        private val singletonInstance: Any,
        private val defaultDeserializer: JsonDeserializer<*>
) : JsonDeserializer<Any>(),
        // Additional interfaces of a specific 'JsonDeserializer' must be supported
        // Kotlin objectInstances are currently handled by a BeanSerializer which
        // implements 'ContextualDeserializer' and 'ResolvableDeserializer'.
        ContextualDeserializer,
        ResolvableDeserializer {

    override fun resolve(ctxt: DeserializationContext?) {
        if (defaultDeserializer is ResolvableDeserializer) {
            defaultDeserializer.resolve(ctxt)
        }
    }

    override fun createContextual(ctxt: DeserializationContext?, property: BeanProperty?): JsonDeserializer<*> =
            if (defaultDeserializer is ContextualDeserializer) {
                defaultDeserializer.createContextual(ctxt, property)
                        .asSingletonDeserializer(singletonInstance)
            } else {
                this
            }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Any {
        defaultDeserializer.deserialize(p, ctxt)
        return singletonInstance
    }
}