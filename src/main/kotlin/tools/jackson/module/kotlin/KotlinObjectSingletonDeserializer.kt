package tools.jackson.module.kotlin

import tools.jackson.core.JsonParser
import tools.jackson.databind.BeanProperty
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

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
