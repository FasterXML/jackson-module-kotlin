package tools.jackson.module.kotlin

import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken.VALUE_NUMBER_INT
import tools.jackson.core.exc.InputCoercionException
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.DeserializationConfig
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.deser.Deserializers
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.deser.std.StdScalarDeserializer
import tools.jackson.databind.exc.InvalidDefinitionException
import tools.jackson.databind.type.LogicalType
import tools.jackson.databind.util.ClassUtil
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.UUID
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaMethod
import kotlin.time.ExperimentalTime
import kotlin.time.Instant as KotlinInstant
import kotlin.time.Duration as KotlinDuration

object SequenceDeserializer : StdDeserializer<Sequence<*>>(Sequence::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Sequence<*> {
        return ctxt.readValue(p, List::class.java).asSequence()
    }
}

object RegexDeserializer : StdDeserializer<Regex>(Regex::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Regex {
        val node = ctxt.readTree(p)

        if (node.isString) {
            return Regex(node.asString())
        } else if (node.isObject) {
            val pattern = node.get("pattern").asString()
            val options = if (node.has("options")) {
                val optionsNode = node.get("options")
                if (!optionsNode.isArray) {
                    throw IllegalStateException("Expected an array of strings for RegexOptions, but type was ${node.nodeType}")
                }
                optionsNode.iterator().asSequence().map { RegexOption.valueOf(it.asString()) }.toSet()
            } else {
                emptySet()
            }
            return Regex(pattern, options)
        } else {
            throw IllegalStateException("Expected a string or an object to deserialize a Regex, but type was ${node.nodeType}")
        }
    }
}

// Unsigned integer deserializers extend StdScalarDeserializer so that polymorphic type information
// (e.g. default typing on an `Any` property) is read as for other scalar values.
object UByteDeserializer : StdScalarDeserializer<UByte>(UByte::class.java) {
    /** @since 3.3 */
    override fun logicalType(): LogicalType = LogicalType.Integer

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.shortValue.asUByte() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.string}) out of range of UByte (0 - ${UByte.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            UByte::class.java
        )
}

object UShortDeserializer : StdScalarDeserializer<UShort>(UShort::class.java) {
    /** @since 3.3 */
    override fun logicalType(): LogicalType = LogicalType.Integer

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.intValue.asUShort() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.string}) out of range of UShort (0 - ${UShort.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            UShort::class.java
        )
}

object UIntDeserializer : StdScalarDeserializer<UInt>(UInt::class.java) {
    /** @since 3.3 */
    override fun logicalType(): LogicalType = LogicalType.Integer

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.longValue.asUInt() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.string}) out of range of UInt (0 - ${UInt.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            UInt::class.java
        )
}

object ULongDeserializer : StdScalarDeserializer<ULong>(ULong::class.java) {
    /** @since 3.3 */
    override fun logicalType(): LogicalType = LogicalType.Integer

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.bigIntegerValue.asULong() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.string}) out of range of ULong (0 - ${ULong.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            ULong::class.java
        )
}

// If the creator does not perform type conversion, implement a unique deserializer for each for fast invocation.
internal sealed class NoConversionCreatorBoxDeserializer<S, D : Any>(
    creator: Method,
    converter: ValueClassBoxConverter<S, D>,
) : WrapsNullableValueClassDeserializer<D>(converter.boxedClass) {
    protected abstract val inputType: Class<*>
    protected val handle: MethodHandle = MethodHandles
        .filterReturnValue(unreflectWithAccessibilityModification(creator), converter.boxHandle)

    // Since the input to handle must be strict, invoke should be implemented in each class
    protected abstract fun invokeExact(value: S): D

    // Cache the result of wrapping null, since the result is always expected to be the same.
    @get:JvmName("boxedNullValue")
    private val boxedNullValue: D by lazy {
        // For the sake of commonality, it is unavoidably called without checking.
        // It is controlled by KotlinValueInstantiator, so it is not expected to reach this branch.
        @Suppress("UNCHECKED_CAST")
        invokeExact(null as S)
    }

    final override fun getBoxedNullValue(): D = boxedNullValue

    final override fun deserialize(p: JsonParser, ctxt: DeserializationContext): D {
        @Suppress("UNCHECKED_CAST")
        return invokeExact(p.readValueAs(inputType) as S)
    }

    internal class WrapsInt<D : Any>(
        creator: Method,
        converter: IntValueClassBoxConverter<D>,
    ) : NoConversionCreatorBoxDeserializer<Int, D>(creator, converter) {
        override val inputType get() = Int::class.java

        @Suppress("UNCHECKED_CAST")
        override fun invokeExact(value: Int): D = handle.invokeExact(value) as D
    }

    internal class WrapsLong<D : Any>(
        creator: Method,
        converter: LongValueClassBoxConverter<D>,
    ) : NoConversionCreatorBoxDeserializer<Long, D>(creator, converter) {
        override val inputType get() = Long::class.java

        @Suppress("UNCHECKED_CAST")
        override fun invokeExact(value: Long): D = handle.invokeExact(value) as D
    }

    internal class WrapsString<D : Any>(
        creator: Method,
        converter: StringValueClassBoxConverter<D>,
    ) : NoConversionCreatorBoxDeserializer<String?, D>(creator, converter) {
        override val inputType get() = String::class.java

        @Suppress("UNCHECKED_CAST")
        override fun invokeExact(value: String?): D = handle.invokeExact(value) as D
    }

    internal class WrapsJavaUuid<D : Any>(
        creator: Method,
        converter: JavaUuidValueClassBoxConverter<D>,
    ) : NoConversionCreatorBoxDeserializer<UUID?, D>(creator, converter) {
        override val inputType get() = UUID::class.java

        @Suppress("UNCHECKED_CAST")
        override fun invokeExact(value: UUID?): D = handle.invokeExact(value) as D
    }

    companion object {
        fun <S, D : Any> create(creator: Method, converter: ValueClassBoxConverter.Specified<S, D>) = when (converter) {
            is IntValueClassBoxConverter -> WrapsInt(creator, converter)
            is LongValueClassBoxConverter -> WrapsLong(creator, converter)
            is StringValueClassBoxConverter -> WrapsString(creator, converter)
            is JavaUuidValueClassBoxConverter -> WrapsJavaUuid(creator, converter)
        }
    }
}

// Even if the creator performs type conversion, it is distinguished
// because a speedup due to rtype matching of filterReturnValue can be expected for the specified type.
internal class HasConversionCreatorWrapsSpecifiedBoxDeserializer<S, D : Any>(
    creator: Method,
    private val inputType: Class<*>,
    converter: ValueClassBoxConverter<S, D>,
) : WrapsNullableValueClassDeserializer<D>(converter.boxedClass) {
    private val handle: MethodHandle

    init {
        val unreflect = unreflectWithAccessibilityModification(creator).run {
            asType(type().changeParameterType(0, Any::class.java))
        }
        handle = MethodHandles.filterReturnValue(unreflect, converter.boxHandle)
    }

    // Cache the result of wrapping null, since the result is always expected to be the same.
    @get:JvmName("boxedNullValue")
    private val boxedNullValue: D by lazy { instantiate(null) }

    override fun getBoxedNullValue(): D = boxedNullValue

    // To instantiate the value class in the same way as other classes,
    // it is necessary to call creator(e.g. constructor-impl) -> box-impl in that order.
    // Input is null only when called from KotlinValueInstantiator.
    @Suppress("UNCHECKED_CAST")
    private fun instantiate(input: Any?): D = handle.invokeExact(input) as D

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): D {
        val input = p.readValueAs(inputType)
        return instantiate(input)
    }
}

internal class WrapsAnyValueClassBoxDeserializer<S, D : Any>(
    creator: Method,
    private val inputType: Class<*>,
    converter: GenericValueClassBoxConverter<S, D>,
) : WrapsNullableValueClassDeserializer<D>(converter.boxedClass) {
    private val handle: MethodHandle

    init {
        val unreflect = unreflectAsTypeWithAccessibilityModification(creator, ANY_TO_ANY_METHOD_TYPE)
        handle = MethodHandles.filterReturnValue(unreflect, converter.boxHandle)
    }

    // Cache the result of wrapping null, since the result is always expected to be the same.
    @get:JvmName("boxedNullValue")
    private val boxedNullValue: D by lazy { instantiate(null) }

    override fun getBoxedNullValue(): D = boxedNullValue

    // To instantiate the value class in the same way as other classes,
    // it is necessary to call creator(e.g. constructor-impl) -> box-impl in that order.
    // Input is null only when called from KotlinValueInstantiator.
    @Suppress("UNCHECKED_CAST")
    private fun instantiate(input: Any?): D = handle.invokeExact(input) as D

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

@OptIn(ExperimentalTime::class)
internal class KotlinDeserializers(
    private val cache: ReflectionCache,
    private val useJavaDurationConversion: Boolean,
    private val useJavaInstantConversion: Boolean,
) : Deserializers.Base() {
    override fun findBeanDeserializer(
        type: JavaType,
        config: DeserializationConfig?,
        beanDescRef: BeanDescription.Supplier?,
    ): ValueDeserializer<*>? {
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
            rawClass == KotlinInstant::class.java ->
                JavaToKotlinInstantConverter.takeIf { useJavaInstantConversion }?.delegatingDeserializer
            rawClass.isUnboxableValueClass() -> findValueCreator(type, rawClass)?.let {
                val unboxedClass = it.returnType
                val converter = cache.getValueClassBoxConverter(unboxedClass, rawClass)

                when (converter) {
                    is ValueClassBoxConverter.Specified -> {
                        val inputType = it.parameterTypes[0]

                        if (inputType == unboxedClass) {
                            NoConversionCreatorBoxDeserializer.create(it, converter)
                        } else {
                            HasConversionCreatorWrapsSpecifiedBoxDeserializer(it, inputType, converter)
                        }
                    }
                    is GenericValueClassBoxConverter ->
                        WrapsAnyValueClassBoxDeserializer(it, it.parameterTypes[0], converter)
                }
            }
            else -> null
        }
    }

    override fun hasDeserializerFor(config: DeserializationConfig, valueType: Class<*>): Boolean {
        return valueType == Sequence::class.java
                || valueType == Regex::class.java
                || valueType == UByte::class.java
                || valueType == UShort::class.java
                || valueType == UInt::class.java
                || valueType == ULong::class.java
                || valueType == KotlinDuration::class.java
                || valueType == KotlinInstant::class.java
    }
}
