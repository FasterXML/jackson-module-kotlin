package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.ser.std.StdSerializer

object SequenceSerializer : StdSerializer<Sequence<*>>(Sequence::class.java) {
    override fun serialize(value: Sequence<*>, gen: JsonGenerator, provider: SerializerProvider) {
        provider.findTypedValueSerializer(
            Iterable::class.java,
            true
        ).serialize(
            value.asIterable(),
            gen,
            provider
        )
    }
}

internal class KotlinSerializers : Serializers.Base() {
    override fun findSerializer(config: SerializationConfig,
                                type: JavaType,
                                beanDesc: BeanDescription?,
                                formatOverrides: JsonFormat.Value?): JsonSerializer<*>? {
        return if (Sequence::class.java.isAssignableFrom(type.rawClass)) {
            SequenceSerializer
        } else {
            null
        }
    }
}