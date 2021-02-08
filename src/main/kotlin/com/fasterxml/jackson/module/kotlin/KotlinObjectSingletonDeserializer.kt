package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ValueDeserializer

internal fun ValueDeserializer<*>.asSingletonDeserializer(singleton: Any) =
        KotlinObjectSingletonDeserializer(singleton, this)

/** deserialize as normal, but return the canonical singleton instance. */
internal class KotlinObjectSingletonDeserializer(
        private val singletonInstance: Any,
        private val defaultDeserializer: ValueDeserializer<*>
) : ValueDeserializer<Any>() {

    override fun resolve(ctxt: DeserializationContext?) {
        defaultDeserializer.resolve(ctxt)
    }

    override fun createContextual(ctxt: DeserializationContext?, property: BeanProperty?): ValueDeserializer<*> =
        defaultDeserializer.createContextual(ctxt, property)
                .asSingletonDeserializer(singletonInstance)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Any {
        defaultDeserializer.deserialize(p, ctxt)
        return singletonInstance
    }
}