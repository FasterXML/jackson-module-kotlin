package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.ser.std.StdSerializer

internal object ValueClassUnboxKeySerializer : StdSerializer<Any>(Any::class.java) {
    override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider) {
        val method = value::class.java.getMethod("unbox-impl")
        val unboxed = method.invoke(value)

        if (unboxed == null) {
            val javaType = provider.typeFactory.constructType(method.genericReturnType)
            provider.findNullKeySerializer(javaType, null).serialize(null, gen, provider)
            return
        }

        provider.findKeySerializer(unboxed::class.java, null).serialize(unboxed, gen, provider)
    }
}

internal class KotlinKeySerializers : Serializers.Base() {
    override fun findSerializer(
        config: SerializationConfig,
        type: JavaType,
        beanDesc: BeanDescription
    ): JsonSerializer<*>? = when {
        type.rawClass.isUnboxableValueClass() -> ValueClassUnboxKeySerializer
        else -> null
    }
}
