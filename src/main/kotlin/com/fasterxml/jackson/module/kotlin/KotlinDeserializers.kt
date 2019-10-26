package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

object SequenceDeserializer : StdDeserializer<Sequence<*>>(Sequence::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Sequence<*> {
        return ctxt.readValue(p, List::class.java).asSequence()
    }
}

internal class KotlinDeserializers: Deserializers.Base() {
    override fun findBeanDeserializer(type: JavaType, config: DeserializationConfig?, beanDesc: BeanDescription?): JsonDeserializer<*>? {
        return if (type.isInterface && type.rawClass == Sequence::class.java) {
            SequenceDeserializer
        } else {
            null
        }
    }
}