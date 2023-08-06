package tools.jackson.module.kotlin

import tools.jackson.databind.JavaType
import tools.jackson.databind.deser.std.StdConvertingDeserializer
import tools.jackson.databind.ser.std.StdDelegatingSerializer
import tools.jackson.databind.type.TypeFactory
import tools.jackson.databind.util.StdConverter
import kotlin.reflect.KClass
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration
import kotlin.time.Duration as KotlinDuration

internal class SequenceToIteratorConverter(private val input: JavaType) : StdConverter<Sequence<*>, Iterator<*>>() {
    override fun convert(value: Sequence<*>): Iterator<*> = value.iterator()

    override fun getInputType(typeFactory: TypeFactory): JavaType = input
    // element-type may not be obtained, so a null check is required
    override fun getOutputType(typeFactory: TypeFactory): JavaType = input.containedType(0)
        ?.let { typeFactory.constructCollectionLikeType(Iterator::class.java, it) }
        ?: typeFactory.constructType(Iterator::class.java)
}

internal object KotlinDurationValueToJavaDurationConverter : StdConverter<Long, JavaDuration>() {
    private val boxConverter by lazy { ValueClassBoxConverter(Long::class.java, KotlinDuration::class) }

    override fun convert(value: Long): JavaDuration = KotlinToJavaDurationConverter.convert(boxConverter.convert(value))
}

internal object KotlinToJavaDurationConverter : StdConverter<KotlinDuration, JavaDuration>() {
    override fun convert(value: KotlinDuration) = value.toJavaDuration()
}

/**
 * Currently it is not possible to deduce type of [kotlin.time.Duration] fields therefore explicit annotation is needed on fields in order to properly deserialize POJO.
 *
 * @see [com.fasterxml.jackson.module.kotlin.test.DurationTests]
 */
internal object JavaToKotlinDurationConverter : StdConverter<JavaDuration, KotlinDuration>() {
    override fun convert(value: JavaDuration) = value.toKotlinDuration()

    val delegatingDeserializer: StdConvertingDeserializer<KotlinDuration> by lazy {
        StdConvertingDeserializer(this)
    }
}

// S is nullable because value corresponds to a nullable value class
// @see KotlinNamesAnnotationIntrospector.findNullSerializer
internal class ValueClassBoxConverter<S : Any?, D : Any>(
    unboxedClass: Class<S>,
    valueClass: KClass<D>
) : StdConverter<S, D>() {
    private val boxMethod = valueClass.java.getDeclaredMethod("box-impl", unboxedClass).apply {
        if (!this.isAccessible) this.isAccessible = true
    }

    @Suppress("UNCHECKED_CAST")
    override fun convert(value: S): D = boxMethod.invoke(null, value) as D

    val delegatingSerializer: StdDelegatingSerializer by lazy { StdDelegatingSerializer(this) }
}
