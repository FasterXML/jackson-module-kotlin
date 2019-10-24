package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

internal fun JsonDeserializer<*>.asSingletonDeserializer(singleton: Any) =
        KotlinObjectSingletonDeserializer(singleton, this)

/** deserialize as normal, but return the canonical singleton instance. */
internal class KotlinObjectSingletonDeserializer(
        private val singletonInstance: Any,
        private val defaultDeserializer: JsonDeserializer<*>
) : JsonDeserializer<Any>() {

    override fun resolve(ctxt: DeserializationContext?) {
        defaultDeserializer.resolve(ctxt)
    }

    override fun createContextual(ctxt: DeserializationContext?, property: BeanProperty?): JsonDeserializer<*> =
        defaultDeserializer.createContextual(ctxt, property)
                .asSingletonDeserializer(singletonInstance)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Any {
        defaultDeserializer.deserialize(p, ctxt)
        return singletonInstance
    }
}