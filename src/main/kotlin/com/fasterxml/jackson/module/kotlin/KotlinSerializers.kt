@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.ValueClassUnboxSerializer.isUnboxableValueClass
import java.math.BigInteger

object SequenceSerializer : StdSerializer<Sequence<*>>(Sequence::class.java) {
    override fun serialize(value: Sequence<*>, gen: JsonGenerator, provider: SerializerProvider) {
        val materializedList = value.toList()
        provider.writeValue(gen, materializedList)
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

object ValueClassUnboxSerializer : StdSerializer<Any>(Any::class.java) {
    override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider) {
        val unboxed = value::class.java.getMethod("unbox-impl").invoke(value)

        if (unboxed == null) {
            gen.writeNull()
            return
        }

        provider.findValueSerializer(unboxed::class.java).serialize(unboxed, gen, provider)
    }

    // In the future, value class without JvmInline will be available, and unbox may not be able to handle it.
    // https://github.com/FasterXML/jackson-module-kotlin/issues/464
    // The JvmInline annotation can be given to Java class,
    // so the isKotlinClass decision is necessary (the order is preferable in terms of possible frequency).
    fun Class<*>.isUnboxableValueClass() = annotations.any { it is JvmInline } && this.isKotlinClass()
}

@Suppress("EXPERIMENTAL_API_USAGE")
internal class KotlinSerializers : Serializers.Base() {
    override fun findSerializer(
        config: SerializationConfig?,
        type: JavaType,
        beanDesc: BeanDescription?,
        formatOverrides: JsonFormat.Value?
    ): ValueSerializer<*>? = when {
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
