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

object UByteSerializer : StdSerializer<UByte>(UByte::class.java) {
    private fun readResolve(): Any = UByteSerializer

    override fun serialize(value: UByte, gen: JsonGenerator, provider: SerializerProvider) =
        gen.writeNumber(value.toShort())
}

object UShortSerializer : StdSerializer<UShort>(UShort::class.java) {
    private fun readResolve(): Any = UShortSerializer

    override fun serialize(value: UShort, gen: JsonGenerator, provider: SerializerProvider) =
        gen.writeNumber(value.toInt())
}

object UIntSerializer : StdSerializer<UInt>(UInt::class.java) {
    private fun readResolve(): Any = UIntSerializer

    override fun serialize(value: UInt, gen: JsonGenerator, provider: SerializerProvider) =
        gen.writeNumber(value.toLong())
}

object ULongSerializer : StdSerializer<ULong>(ULong::class.java) {
    private fun readResolve(): Any = ULongSerializer

    override fun serialize(value: ULong, gen: JsonGenerator, provider: SerializerProvider) {
        val longValue = value.toLong()
        when {
            longValue >= 0 -> gen.writeNumber(longValue)
            else -> gen.writeNumber(BigInteger(value.toString()))
        }
    }
}

// Class must be UnboxableValueClass.
private fun Class<*>.getStaticJsonValueGetter(): Method? = this.declaredMethods.find { method ->
    Modifier.isStatic(method.modifiers) && method.annotations.any { it is JsonValue && it.value }
}

object ValueClassUnboxSerializer : StdSerializer<Any>(Any::class.java) {
    private fun readResolve(): Any = ValueClassUnboxSerializer

    override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider) {
        val unboxed = value::class.java.getMethod("unbox-impl").invoke(value)
        provider.defaultSerializeValue(unboxed, gen)
    }
}

internal sealed class ValueClassSerializer<T : Any>(t: Class<T>) : StdSerializer<T>(t) {
    class StaticJsonValue<T : Any>(
        t: Class<T>, private val staticJsonValueGetter: Method
    ) : ValueClassSerializer<T>(t) {
        private val unboxMethod: Method = t.getMethod("unbox-impl")

        override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
            val unboxed = unboxMethod.invoke(value)
            // As shown in the processing of the factory function, jsonValueGetter is always a static method.
            val jsonValue: Any? = staticJsonValueGetter.invoke(null, unboxed)
            provider.defaultSerializeValue(jsonValue, gen)
        }
    }

    companion object {
        // `t` must be UnboxableValueClass.
        // If create a function with a JsonValue in the value class,
        // it will be compiled as a static method (= cannot be processed properly by Jackson),
        // so use a ValueClassSerializer.StaticJsonValue to handle this.
        fun from(t: Class<*>): StdSerializer<*> = t.getStaticJsonValueGetter()
            ?.let { StaticJsonValue(t, it) }
            ?: ValueClassUnboxSerializer
    }
}

internal class KotlinSerializers : Serializers.Base() {
    override fun findSerializer(
        config: SerializationConfig?,
        type: JavaType,
        beanDesc: BeanDescription?
    ): JsonSerializer<*>? {
        val rawClass = type.rawClass

        return when {
            UByte::class.java == rawClass -> UByteSerializer
            UShort::class.java == rawClass -> UShortSerializer
            UInt::class.java == rawClass -> UIntSerializer
            ULong::class.java == rawClass -> ULongSerializer
            // The priority of Unboxing needs to be lowered so as not to break the serialization of Unsigned Integers.
            rawClass.isUnboxableValueClass() -> ValueClassSerializer.from(rawClass)
            else -> null
        }
    }
}
