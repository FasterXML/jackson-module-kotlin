package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT
import com.fasterxml.jackson.core.exc.InputCoercionException
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.databind.util.ClassUtil
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaMethod
import kotlin.time.Duration as KotlinDuration

object SequenceDeserializer : StdDeserializer<Sequence<*>>(Sequence::class.java) {
    private fun readResolve(): Any = SequenceDeserializer

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Sequence<*> {
        return ctxt.readValue(p, List::class.java).asSequence()
    }
}

object RegexDeserializer : StdDeserializer<Regex>(Regex::class.java) {
    private fun readResolve(): Any = RegexDeserializer

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Regex {
        val node = ctxt.readTree(p)

        if (node.isTextual) {
            return Regex(node.asText())
        } else if (node.isObject) {
            val pattern = node.get("pattern").asText()
            val options = if (node.has("options")) {
                val optionsNode = node.get("options")
                if (!optionsNode.isArray) {
                    throw IllegalStateException("Expected an array of strings for RegexOptions, but type was ${node.nodeType}")
                }
                optionsNode.elements().asSequence().map { RegexOption.valueOf(it.asText()) }.toSet()
            } else {
                emptySet()
            }
            return Regex(pattern, options)
        } else {
            throw IllegalStateException("Expected a string or an object to deserialize a Regex, but type was ${node.nodeType}")
        }
    }
}

object UByteDeserializer : StdDeserializer<UByte>(UByte::class.java) {
    private fun readResolve(): Any = UByteDeserializer

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.shortValue.asUByte() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.text}) out of range of UByte (0 - ${UByte.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            UByte::class.java
        )
}

object UShortDeserializer : StdDeserializer<UShort>(UShort::class.java) {
    private fun readResolve(): Any = UShortDeserializer

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.intValue.asUShort() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.text}) out of range of UShort (0 - ${UShort.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            UShort::class.java
        )
}

object UIntDeserializer : StdDeserializer<UInt>(UInt::class.java) {
    private fun readResolve(): Any = UIntDeserializer

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.longValue.asUInt() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.text}) out of range of UInt (0 - ${UInt.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            UInt::class.java
        )
}

object ULongDeserializer : StdDeserializer<ULong>(ULong::class.java) {
    private fun readResolve(): Any = ULongDeserializer

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.bigIntegerValue.asULong() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.text}) out of range of ULong (0 - ${ULong.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            ULong::class.java
        )
}

internal class WrapsNullableValueClassBoxDeserializer<S, D : Any>(
    private val creator: Method,
    private val converter: ValueClassBoxConverter<S, D>
) : WrapsNullableValueClassDeserializer<D>(converter.boxedClass) {
    private val inputType: Class<*> = creator.parameterTypes[0]

    init {
        ClassUtil.checkAndFixAccess(creator, false)
    }

    // Cache the result of wrapping null, since the result is always expected to be the same.
    @get:JvmName("boxedNullValue")
    private val boxedNullValue: D by lazy { instantiate(null) }

    override fun getBoxedNullValue(): D = boxedNullValue

    // To instantiate the value class in the same way as other classes,
    // it is necessary to call creator(e.g. constructor-impl) -> box-impl in that order.
    // Input is null only when called from KotlinValueInstantiator.
    @Suppress("UNCHECKED_CAST")
    private fun instantiate(input: Any?): D = converter.convert(creator.invoke(null, input) as S)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): D {
        val input = p.readValueAs(inputType)
        return instantiate(input)
    }
}

private fun invalidCreatorMessage(m: Method): String =
    "The argument size of a Creator that does not return a value class on the JVM must be 1, " +
            "please fix it or use JsonDeserializer.\n" +
            "Detected: ${m.parameters.joinToString(prefix = "${m.name}(", separator = ", ", postfix = ")") { it.name }}"

private fun findValueCreator(type: JavaType, clazz: Class<*>): Method? {
    clazz.declaredMethods.forEach { method ->
        if (Modifier.isStatic(method.modifiers) && method.hasCreatorAnnotation()) {
            // Do nothing if a correctly functioning Creator is defined
            return method.takeIf { clazz != method.returnType }?.apply {
                // Creator with an argument size not equal to 1 is currently not supported.
                if (parameterCount != 1) {
                    throw InvalidDefinitionException.from(null as JsonParser?, invalidCreatorMessage(method), type)
                }
            }
        }
    }

    // primaryConstructor.javaMethod for the value class returns constructor-impl
    return clazz.kotlin.primaryConstructor?.javaMethod
}

internal class KotlinDeserializers(
    private val cache: ReflectionCache,
    private val useJavaDurationConversion: Boolean,
) : Deserializers.Base() {
    override fun findBeanDeserializer(
        type: JavaType,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?,
    ): JsonDeserializer<*>? {
        val rawClass = type.rawClass

        return when {
            type.isInterface && rawClass == Sequence::class.java -> SequenceDeserializer
            rawClass == Regex::class.java -> RegexDeserializer
            rawClass == UByte::class.java -> UByteDeserializer
            rawClass == UShort::class.java -> UShortDeserializer
            rawClass == UInt::class.java -> UIntDeserializer
            rawClass == ULong::class.java -> ULongDeserializer
            rawClass == KotlinDuration::class.java ->
                JavaToKotlinDurationConverter.takeIf { useJavaDurationConversion }?.delegatingDeserializer
            rawClass.isUnboxableValueClass() -> findValueCreator(type, rawClass)?.let {
                val unboxedClass = it.returnType
                val converter = cache.getValueClassBoxConverter(unboxedClass, rawClass.kotlin)
                WrapsNullableValueClassBoxDeserializer(it, converter)
            }
            else -> null
        }
    }
}
