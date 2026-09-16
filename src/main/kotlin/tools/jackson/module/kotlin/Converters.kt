package tools.jackson.module.kotlin

import tools.jackson.databind.JavaType
import tools.jackson.databind.deser.std.StdConvertingDeserializer
import tools.jackson.databind.ser.std.StdDelegatingSerializer
import tools.jackson.databind.type.TypeFactory
import tools.jackson.databind.util.ClassUtil
import tools.jackson.databind.util.StdConverter
import kotlin.reflect.KClass
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaDuration
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinDuration
import kotlin.time.toKotlinInstant
import java.time.Duration as JavaDuration
import java.time.Instant as JavaInstant
import kotlin.time.Duration as KotlinDuration
import kotlin.time.Instant as KotlinInstant

internal class SequenceToIteratorConverter(private val input: JavaType) : StdConverter<Sequence<*>, Iterator<*>>() {
    override fun convert(value: Sequence<*>): Iterator<*> = value.iterator()

    override fun getInputType(typeFactory: TypeFactory): JavaType = input
    // element-type may not be obtained, so a null check is required
    override fun getOutputType(typeFactory: TypeFactory): JavaType = input.containedType(0)
        ?.let { typeFactory.constructCollectionLikeType(Iterator::class.java, it) }
        ?: typeFactory.constructType(Iterator::class.java)
}

internal object KotlinDurationValueToJavaDurationConverter : StdConverter<Long, JavaDuration>() {
    private val boxConverter by lazy { LongValueClassBoxConverter(KotlinDuration::class.java) }

    override fun convert(value: Long): JavaDuration = KotlinToJavaDurationConverter.convert(boxConverter.convert(value))
}

internal object KotlinToJavaDurationConverter : StdConverter<KotlinDuration, JavaDuration>() {
    override fun convert(value: KotlinDuration) = value.toJavaDuration()
}

/**
 * Currently it is not possible to deduce type of [kotlin.time.Duration] fields therefore explicit annotation is needed on fields in order to properly deserialize POJO.
 *
 * @see [tools.jackson.module.kotlin.test.DurationTests]
 */
internal object JavaToKotlinDurationConverter : StdConverter<JavaDuration, KotlinDuration>() {
    override fun convert(value: JavaDuration) = value.toKotlinDuration()

    val delegatingDeserializer: StdConvertingDeserializer<KotlinDuration> by lazy {
        StdConvertingDeserializer(this)
    }
}

@OptIn(ExperimentalTime::class)
internal object KotlinToJavaInstantConverter : StdConverter<KotlinInstant, JavaInstant>() {
    override fun convert(value: KotlinInstant) = value.toJavaInstant()
}

@OptIn(ExperimentalTime::class)
internal object JavaToKotlinInstantConverter : StdConverter<JavaInstant, KotlinInstant>() {
    override fun convert(value: JavaInstant) = value.toKotlinInstant()

    val delegatingDeserializer: StdConvertingDeserializer<KotlinInstant> by lazy {
        StdConvertingDeserializer(this)
    }
}

internal sealed class ValueClassBoxConverter<S : Any?, D : Any> : StdConverter<S, D>() {
    abstract val boxedClass: Class<D>
    abstract val boxHandle: MethodHandle

    protected fun rawBoxHandle(
        unboxedClass: Class<*>,
    ): MethodHandle = MethodHandles.lookup().findStatic(
        boxedClass,
        "box-impl",
        MethodType.methodType(boxedClass, unboxedClass),
    )

    val delegatingSerializer: StdDelegatingSerializer by lazy { StdDelegatingSerializer(this) }

    companion object {
        fun create(
            unboxedClass: Class<*>,
            valueClass: Class<*>,
        ): ValueClassBoxConverter<*, *> = when (unboxedClass) {
            Int::class.java -> IntValueClassBoxConverter(valueClass)
            Long::class.java -> LongValueClassBoxConverter(valueClass)
            String::class.java -> StringValueClassBoxConverter(valueClass)
            UUID::class.java -> JavaUuidValueClassBoxConverter(valueClass)
            else -> GenericValueClassBoxConverter(unboxedClass, valueClass)
        }
    }

    // If the wrapped type is explicitly specified, it is inherited for the sake of distinction
    internal sealed class Specified<S : Any?, D : Any> : ValueClassBoxConverter<S, D>()
}

// region: Converters for common classes as wrapped values, add as needed.
internal class IntValueClassBoxConverter<D : Any>(
    override val boxedClass: Class<D>,
) : ValueClassBoxConverter.Specified<Int, D>() {
    override val boxHandle: MethodHandle = rawBoxHandle(Int::class.java).asType(INT_TO_ANY_METHOD_TYPE)

    @Suppress("UNCHECKED_CAST")
    override fun convert(value: Int): D = boxHandle.invokeExact(value) as D
}

internal class LongValueClassBoxConverter<D : Any>(
    override val boxedClass: Class<D>,
) : ValueClassBoxConverter.Specified<Long, D>() {
    override val boxHandle: MethodHandle = rawBoxHandle(Long::class.java).asType(LONG_TO_ANY_METHOD_TYPE)

    @Suppress("UNCHECKED_CAST")
    override fun convert(value: Long): D = boxHandle.invokeExact(value) as D
}

internal class StringValueClassBoxConverter<D : Any>(
    override val boxedClass: Class<D>,
) : ValueClassBoxConverter.Specified<String?, D>() {
    override val boxHandle: MethodHandle = rawBoxHandle(String::class.java).asType(STRING_TO_ANY_METHOD_TYPE)

    @Suppress("UNCHECKED_CAST")
    override fun convert(value: String?): D = boxHandle.invokeExact(value) as D
}

internal class JavaUuidValueClassBoxConverter<D : Any>(
    override val boxedClass: Class<D>,
) : ValueClassBoxConverter.Specified<UUID?, D>() {
    override val boxHandle: MethodHandle = rawBoxHandle(UUID::class.java).asType(JAVA_UUID_TO_ANY_METHOD_TYPE)

    @Suppress("UNCHECKED_CAST")
    override fun convert(value: UUID?): D = boxHandle.invokeExact(value) as D
}
// endregion

/**
 * A converter that only performs box processing for the value class.
 * Note that constructor-impl is not called.
 * @param S is nullable because value corresponds to a nullable value class.
 *   see [io.github.projectmapk.jackson.module.kogera.annotationIntrospector.KotlinFallbackAnnotationIntrospector.findNullSerializer]
 */
internal class GenericValueClassBoxConverter<S : Any?, D : Any>(
    unboxedClass: Class<S>,
    override val boxedClass: Class<D>,
) : ValueClassBoxConverter<S, D>() {
    override val boxHandle: MethodHandle = rawBoxHandle(unboxedClass).asType(ANY_TO_ANY_METHOD_TYPE)

    @Suppress("UNCHECKED_CAST")
    override fun convert(value: S): D = boxHandle.invokeExact(value) as D
}

internal sealed class ValueClassUnboxConverter<S : Any, D : Any?> : StdConverter<S, D>() {
    abstract val valueClass: Class<S>
    abstract val unboxedType: Type
    abstract val unboxHandle: MethodHandle

    final override fun getInputType(typeFactory: TypeFactory): JavaType = typeFactory.constructType(valueClass)
    final override fun getOutputType(typeFactory: TypeFactory): JavaType = typeFactory.constructType(unboxedType)

    val serializer: ValueClassUnboxSerializer by lazy { ValueClassUnboxSerializer(this) }

    companion object {
        fun create(valueClass: Class<*>): ValueClassUnboxConverter<*, *> {
            val unboxMethod = valueClass.getDeclaredMethod("unbox-impl")
            val unboxedType = unboxMethod.genericReturnType

            return when (unboxedType) {
                Int::class.java -> IntValueClassUnboxConverter(valueClass, unboxMethod)
                Long::class.java -> LongValueClassUnboxConverter(valueClass, unboxMethod)
                String::class.java -> StringValueClassUnboxConverter(valueClass, unboxMethod)
                UUID::class.java -> JavaUuidValueClassUnboxConverter(valueClass, unboxMethod)
                else -> GenericValueClassUnboxConverter(valueClass, unboxedType, unboxMethod)
            }
        }
    }
}

internal class IntValueClassUnboxConverter<T : Any>(
    override val valueClass: Class<T>,
    unboxMethod: Method,
) : ValueClassUnboxConverter<T, Int>() {
    override val unboxedType: Type get() = Int::class.java
    override val unboxHandle: MethodHandle = unreflectAsTypeWithAccessibilityModification(unboxMethod, ANY_TO_INT_METHOD_TYPE)

    override fun convert(value: T): Int = unboxHandle.invokeExact(value) as Int
}

internal class LongValueClassUnboxConverter<T : Any>(
    override val valueClass: Class<T>,
    unboxMethod: Method,
) : ValueClassUnboxConverter<T, Long>() {
    override val unboxedType: Type get() = Long::class.java
    override val unboxHandle: MethodHandle = unreflectAsTypeWithAccessibilityModification(unboxMethod, ANY_TO_LONG_METHOD_TYPE)

    override fun convert(value: T): Long = unboxHandle.invokeExact(value) as Long
}

internal class StringValueClassUnboxConverter<T : Any>(
    override val valueClass: Class<T>,
    unboxMethod: Method,
) : ValueClassUnboxConverter<T, String?>() {
    override val unboxedType: Type get() = String::class.java
    override val unboxHandle: MethodHandle = unreflectAsTypeWithAccessibilityModification(unboxMethod, ANY_TO_STRING_METHOD_TYPE)

    override fun convert(value: T): String? = unboxHandle.invokeExact(value) as String?
}

internal class JavaUuidValueClassUnboxConverter<T : Any>(
    override val valueClass: Class<T>,
    unboxMethod: Method,
) : ValueClassUnboxConverter<T, UUID?>() {
    override val unboxedType: Type get() = UUID::class.java
    override val unboxHandle: MethodHandle = unreflectAsTypeWithAccessibilityModification(unboxMethod, ANY_TO_JAVA_UUID_METHOD_TYPE)

    override fun convert(value: T): UUID? = unboxHandle.invokeExact(value) as UUID?
}

internal class GenericValueClassUnboxConverter<T : Any>(
    override val valueClass: Class<T>,
    override val unboxedType: Type,
    unboxMethod: Method,
) : ValueClassUnboxConverter<T, Any?>() {
    override val unboxHandle: MethodHandle = unreflectAsTypeWithAccessibilityModification(unboxMethod, ANY_TO_ANY_METHOD_TYPE)

    override fun convert(value: T): Any? = unboxHandle.invokeExact(value)
}
