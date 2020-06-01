package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.SingletonSupport.CANONICALIZE
import com.fasterxml.jackson.module.kotlin.SingletonSupport.DISABLED
import kotlin.reflect.KClass

private val metadataFqName = "kotlin.Metadata"

fun Class<*>.isKotlinClass(): Boolean {
    return declaredAnnotations.any { it.annotationClass.java.name == metadataFqName }
}

class KotlinModule constructor (
    val reflectionCacheSize: Int = 512,
    val nullToEmptyCollection: Boolean = false,
    val nullToEmptyMap: Boolean = false,
    val nullIsSameAsDefault: Boolean = false,
    val singletonSupport: SingletonSupport = DISABLED
) : SimpleModule(PackageVersion.VERSION) {
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "For ABI compatibility")
    constructor(
        reflectionCacheSize: Int = 512,
        nullToEmptyCollection: Boolean = false,
        nullToEmptyMap: Boolean = false
    ) : this(reflectionCacheSize, nullToEmptyCollection, nullToEmptyMap, false)

    @Deprecated(level = DeprecationLevel.HIDDEN, message = "For ABI compatibility")
    constructor(
        reflectionCacheSize: Int = 512,
        nullToEmptyCollection: Boolean = false,
        nullToEmptyMap: Boolean = false,
        nullIsSameAsDefault: Boolean = false
    ) : this(reflectionCacheSize, nullToEmptyCollection, nullToEmptyMap, nullIsSameAsDefault)

    private constructor(builder: Builder) : this(
        builder.reflectionCacheSize,
        builder.nullToEmptyCollection,
        builder.nullToEmptyMap,
        builder.nullIsSameAsDefault,
        builder.singletonSupport
    )

    companion object {
        const val serialVersionUID = 1L
    }

    private val ignoredClassesForImplyingJsonCreator = emptySet<KClass<*>>()

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        if (!context.isEnabled(MapperFeature.USE_ANNOTATIONS)) {
            throw IllegalStateException("The Jackson Kotlin module requires USE_ANNOTATIONS to be true or it cannot function")
        }

        val cache = ReflectionCache(reflectionCacheSize)

        context.addValueInstantiators(KotlinInstantiators(cache, nullToEmptyCollection, nullToEmptyMap, nullIsSameAsDefault))

        when(singletonSupport) {
            DISABLED -> Unit
            CANONICALIZE -> {
                context.addBeanDeserializerModifier(KotlinBeanDeserializerModifier)
            }
        }

        context.insertAnnotationIntrospector(KotlinAnnotationIntrospector(context, cache, nullToEmptyCollection, nullToEmptyMap, nullIsSameAsDefault))
        context.appendAnnotationIntrospector(KotlinNamesAnnotationIntrospector(this, cache, ignoredClassesForImplyingJsonCreator))

        context.addDeserializers(KotlinDeserializers())
        context.addSerializers(KotlinSerializers())

        fun addMixIn(clazz: Class<*>, mixin: Class<*>) {
            context.setMixInAnnotations(clazz, mixin)
        }

        // ranges
        addMixIn(IntRange::class.java, ClosedRangeMixin::class.java)
        addMixIn(CharRange::class.java, ClosedRangeMixin::class.java)
        addMixIn(LongRange::class.java, ClosedRangeMixin::class.java)
        addMixIn(ClosedRange::class.java, ClosedRangeMixin::class.java)
    }

    class Builder {
        var reflectionCacheSize: Int = 512
            private set

        var nullToEmptyCollection: Boolean = false
            private set

        var nullToEmptyMap: Boolean = false
            private set

        var nullIsSameAsDefault: Boolean = false
            private set

        var singletonSupport = DISABLED
            private set

        fun reflectionCacheSize(reflectionCacheSize: Int) = apply { this.reflectionCacheSize = reflectionCacheSize }

        fun nullToEmptyCollection(nullToEmptyCollection: Boolean) =
            apply { this.nullToEmptyCollection = nullToEmptyCollection }

        fun nullToEmptyMap(nullToEmptyMap: Boolean) = apply { this.nullToEmptyMap = nullToEmptyMap }

        fun nullIsSameAsDefault(nullIsSameAsDefault: Boolean) = apply { this.nullIsSameAsDefault = nullIsSameAsDefault }

        fun singletonSupport(singletonSupport: SingletonSupport) =
            apply { this.singletonSupport = singletonSupport }

        fun build() = KotlinModule(this)
    }
}


