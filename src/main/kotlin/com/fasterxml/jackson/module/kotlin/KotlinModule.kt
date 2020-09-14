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

/**
 * @param   reflectionCacheSize     Default: 512.  Size, in items, of the caches used for mapping objects.
 * @param   nullToEmptyCollection   Default: false.  Whether to deserialize null values for collection properties as
 *                                      empty collections.
 * @param   nullToEmptyMap          Default: false.  Whether to deserialize null values for a map property to an empty
 *                                      map object.
 * @param   nullIsSameAsDefault     Default false.  Whether to treat null values as absent when deserializing, thereby
 *                                      using the default value provided in Kotlin.
 * @param   singletonSupport        Default: DISABLED.  Mode for singleton handling.
 *                                      See {@link com.fasterxml.jackson.module.kotlin.SingletonSupport label}
 * @param   strictNullChecks        Default: false.  Whether to check deserialized collections.  With this disabled,
 *                                      the default, collections which are typed to disallow null members
 *                                      (e.g. List<String>) may contain null values after deserialization.  Enabling it
 *                                      protects against this but has significant performance impact.
 */
class KotlinModule constructor (
    val reflectionCacheSize: Int = 512,
    val nullToEmptyCollection: Boolean = false,
    val nullToEmptyMap: Boolean = false,
    val nullIsSameAsDefault: Boolean = false,
    val singletonSupport: SingletonSupport = DISABLED,
    val strictNullChecks: Boolean = false
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
        builder.singletonSupport,
        builder.strictNullChecks
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

        context.addValueInstantiators(KotlinInstantiators(cache, nullToEmptyCollection, nullToEmptyMap, nullIsSameAsDefault, strictNullChecks))

        when(singletonSupport) {
            DISABLED -> Unit
            CANONICALIZE -> {
 	        // [module-kotlin#225]: keep Kotlin singletons as singletons
                context.addDeserializerModifier(KotlinBeanDeserializerModifier)
            }
        }

        context.insertAnnotationIntrospector(KotlinAnnotationIntrospector(context, cache, nullToEmptyCollection, nullToEmptyMap, nullIsSameAsDefault))
        context.appendAnnotationIntrospector(KotlinNamesAnnotationIntrospector(this, cache, ignoredClassesForImplyingJsonCreator))

        context.addDeserializers(KotlinDeserializers())
        context.addSerializers(KotlinSerializers())

        fun addMixIn(clazz: Class<*>, mixin: Class<*>) {
            context.setMixIn(clazz, mixin)
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

        var strictNullChecks = false
            private set

        fun reflectionCacheSize(reflectionCacheSize: Int) = apply { this.reflectionCacheSize = reflectionCacheSize }

        fun nullToEmptyCollection(nullToEmptyCollection: Boolean) =
            apply { this.nullToEmptyCollection = nullToEmptyCollection }

        fun nullToEmptyMap(nullToEmptyMap: Boolean) = apply { this.nullToEmptyMap = nullToEmptyMap }

        fun nullIsSameAsDefault(nullIsSameAsDefault: Boolean) = apply { this.nullIsSameAsDefault = nullIsSameAsDefault }

        fun singletonSupport(singletonSupport: SingletonSupport) =
            apply { this.singletonSupport = singletonSupport }

        fun strictNullChecks(strictNullChecks: Boolean) =
                apply { this.strictNullChecks = strictNullChecks }

        fun build() = KotlinModule(this)
    }
}
