package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.util.StdConverter
import kotlin.reflect.KClass
import kotlin.time.toJavaDuration
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

internal object KotlinToJavaDurationConverter : StdConverter<KotlinDuration, JavaDuration>() {
    override fun convert(value: KotlinDuration) = value.toJavaDuration()
}

// this class is needed as workaround for deserialization
// data classes with kotlin.time.Duration field which is a value class
//
// @see DurationTests.`should deserialize Kotlin duration inside data class`
object JavaToKotlinDurationConverter : StdConverter<JavaDuration, KotlinDuration>() {
    override fun convert(value: JavaDuration) = KotlinDuration.parseIsoString(value.toString())
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
