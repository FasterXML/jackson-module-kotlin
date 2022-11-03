package tools.jackson.module.kotlin

import tools.jackson.core.JsonParser
import tools.jackson.databind.BeanProperty
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonDeserializer
import tools.jackson.databind.deser.ContextualDeserializer
import tools.jackson.databind.deser.ResolvableDeserializer

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