package tools.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonValue
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.JavaType
import tools.jackson.databind.SerializationConfig
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.ser.Serializers
import tools.jackson.databind.ser.std.StdSerializer
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.math.BigInteger

object UByteSerializer : StdSerializer<UByte>(UByte::class.java) {
    override fun serialize(value: UByte, gen: JsonGenerator, ctxt: SerializationContext) {
        gen.writeNumber(value.toShort())
    }
}

object UShortSerializer : StdSerializer<UShort>(UShort::class.java) {
    override fun serialize(value: UShort, gen: JsonGenerator, ctxt: SerializationContext) {
        gen.writeNumber(value.toInt())
    }
}

object UIntSerializer : StdSerializer<UInt>(UInt::class.java) {
    override fun serialize(value: UInt, gen: JsonGenerator, ctxt: SerializationContext) {
        gen.writeNumber(value.toLong())
    }
}

object ULongSerializer : StdSerializer<ULong>(ULong::class.java) {
    override fun serialize(value: ULong, gen: JsonGenerator, ctxt: SerializationContext) {
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
    override fun serialize(value: Any, gen: JsonGenerator, ctxt: SerializationContext) {
        val unboxed = value::class.java.getMethod("unbox-impl").invoke(value)

        if (unboxed == null) {
            ctxt.findNullValueSerializer(null).serialize(null, gen, ctxt)
            return
        }

        ctxt.writeValue(gen, unboxed)
    }
}

internal sealed class ValueClassSerializer<T : Any>(t: Class<T>) : StdSerializer<T>(t) {
    class StaticJsonValue<T : Any>(
        t: Class<T>, private val staticJsonValueGetter: Method
    ) : ValueClassSerializer<T>(t) {
        private val unboxMethod: Method = t.getMethod("unbox-impl")

        override fun serialize(value: T, gen: JsonGenerator, ctxt: SerializationContext) {
            val unboxed = unboxMethod.invoke(value)
            // As shown in the processing of the factory function, jsonValueGetter is always a static method.
            val jsonValue: Any? = staticJsonValueGetter.invoke(null, unboxed)
            jsonValue
                ?.let { ctxt.findValueSerializer(it::class.java).serialize(it, gen, ctxt) }
                ?: ctxt.findNullValueSerializer(null).serialize(null, gen, ctxt)
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
        beanDesc: BeanDescription?,
        formatOverrides: JsonFormat.Value?
    ): ValueSerializer<*>? {
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
