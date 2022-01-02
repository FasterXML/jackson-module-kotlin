package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.math.BigInteger

object SequenceSerializer : StdSerializer<Sequence<*>>(Sequence::class.java) {
    override fun serialize(value: Sequence<*>, gen: JsonGenerator, provider: SerializerProvider) {
        val materializedList = value.toList()
        provider.defaultSerializeValue(materializedList, gen)
    }
}

object UByteSerializer : StdSerializer<UByte>(UByte::class.java) {
    override fun serialize(value: UByte, gen: JsonGenerator, provider: SerializerProvider) =
        gen.writeNumber(value.toShort())
}

object UShortSerializer : StdSerializer<UShort>(UShort::class.java) {
    override fun serialize(value: UShort, gen: JsonGenerator, provider: SerializerProvider) =
        gen.writeNumber(value.toInt())
}

object UIntSerializer : StdSerializer<UInt>(UInt::class.java) {
    override fun serialize(value: UInt, gen: JsonGenerator, provider: SerializerProvider) =
        gen.writeNumber(value.toLong())
}

object ULongSerializer : StdSerializer<ULong>(ULong::class.java) {
    override fun serialize(value: ULong, gen: JsonGenerator, provider: SerializerProvider) {
        val longValue = value.toLong()
        when {
            longValue >= 0 -> gen.writeNumber(longValue)
            else -> gen.writeNumber(BigInteger(value.toString()))
        }
    }
}

// Class must be UnboxableValueClass.
private fun Class<*>.getStaticJsonValueGetter(): Method? = this.declaredMethods
    .find { method -> Modifier.isStatic(method.modifiers) && method.annotations.any { it is JsonValue } }

object ValueClassUnboxSerializer : StdSerializer<Any>(Any::class.java) {
    override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider) {
        val unboxed = value::class.java.getMethod("unbox-impl").invoke(value)

        if (unboxed == null) {
            provider.findNullValueSerializer(null).serialize(unboxed, gen, provider)
            return
        }

        provider.findValueSerializer(unboxed::class.java).serialize(unboxed, gen, provider)
    }
}

internal class KotlinSerializers : Serializers.Base() {
    override fun findSerializer(
        config: SerializationConfig?,
        type: JavaType,
        beanDesc: BeanDescription?
    ): JsonSerializer<*>? = when {
        Sequence::class.java.isAssignableFrom(type.rawClass) -> SequenceSerializer
        UByte::class.java.isAssignableFrom(type.rawClass) -> UByteSerializer
        UShort::class.java.isAssignableFrom(type.rawClass) -> UShortSerializer
        UInt::class.java.isAssignableFrom(type.rawClass) -> UIntSerializer
        ULong::class.java.isAssignableFrom(type.rawClass) -> ULongSerializer
        // The priority of Unboxing needs to be lowered so as not to break the serialization of Unsigned Integers.
        type.rawClass.isUnboxableValueClass() -> ValueClassUnboxSerializer
        else -> null
    }
}

// This serializer is used to properly serialize the value class.
// The getter generated for the value class is special,
// so this class will not work properly when added to the Serializers
// (it is configured from KotlinAnnotationIntrospector.findSerializer).
internal class ValueClassBoxSerializer<T : Any>(
    private val outerClazz: Class<out Any>, innerClazz: Class<T>
) : StdSerializer<T>(innerClazz) {
    private val boxMethod = outerClazz.getMethod("box-impl", innerClazz)

    override fun serialize(value: T?, gen: JsonGenerator, provider: SerializerProvider) {
        // Values retrieved from getter are considered validated and constructor-impl is not executed.
        val boxed = boxMethod.invoke(null, value)

        provider.findValueSerializer(outerClazz).serialize(boxed, gen, provider)
    }
}

internal class ValueClassStaticJsonValueSerializer<T> private constructor(
    innerClazz: Class<T>,
    private val staticJsonValueGetter: Method
) : StdSerializer<T>(innerClazz) {
    override fun serialize(value: T?, gen: JsonGenerator, provider: SerializerProvider) {
        // As shown in the processing of the factory function, jsonValueGetter is always a static method.
        val jsonValue: Any? = staticJsonValueGetter.invoke(null, value)
        jsonValue
            ?.let { provider.findValueSerializer(it::class.java).serialize(it, gen, provider) }
            ?: provider.findNullValueSerializer(null).serialize(null, gen, provider)
    }

    // Since JsonValue can be processed correctly if it is given to a non-static getter/field,
    // this class will only process if it is a `static` method.
    companion object {
        fun <T> createdOrNull(
            outerClazz: Class<out Any>,
            innerClazz: Class<T>
        ): ValueClassStaticJsonValueSerializer<T>? = outerClazz
            .getStaticJsonValueGetter()
            ?.let { ValueClassStaticJsonValueSerializer(innerClazz, it) }
    }
}
