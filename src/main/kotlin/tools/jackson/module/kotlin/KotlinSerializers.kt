package tools.jackson.module.kotlin

import tools.jackson.annotation.JsonValue
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.JavaType
import tools.jackson.databind.JsonSerializer
import tools.jackson.databind.SerializationConfig
import tools.jackson.databind.SerializerProvider
import tools.jackson.databind.ser.Serializers
import tools.jackson.databind.ser.std.StdSerializer
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
            provider.findNullValueSerializer(null).serialize(null, gen, provider)
            return
        }

        provider.findValueSerializer(unboxed::class.java).serialize(unboxed, gen, provider)
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
            jsonValue
                ?.let { provider.findValueSerializer(it::class.java).serialize(it, gen, provider) }
                ?: provider.findNullValueSerializer(null).serialize(null, gen, provider)
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
            Sequence::class.java.isAssignableFrom(rawClass) -> SequenceSerializer
            UByte::class.java.isAssignableFrom(rawClass) -> UByteSerializer
            UShort::class.java.isAssignableFrom(rawClass) -> UShortSerializer
            UInt::class.java.isAssignableFrom(rawClass) -> UIntSerializer
            ULong::class.java.isAssignableFrom(rawClass) -> ULongSerializer
            // The priority of Unboxing needs to be lowered so as not to break the serialization of Unsigned Integers.
            rawClass.isUnboxableValueClass() -> ValueClassSerializer.from(rawClass)
            else -> null
        }
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
