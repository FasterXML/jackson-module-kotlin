package tools.jackson.module.kotlin

import tools.jackson.databind.MapperFeature
import tools.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullIsSameAsDefault
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyCollection
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyMap
import com.fasterxml.jackson.module.kotlin.KotlinFeature.StrictNullChecks
import com.fasterxml.jackson.module.kotlin.SingletonSupport.CANONICALIZE
import com.fasterxml.jackson.module.kotlin.SingletonSupport.DISABLED
import java.util.*
import kotlin.reflect.KClass

private const val metadataFqName = "kotlin.Metadata"

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
class KotlinModule @Deprecated(
    level = DeprecationLevel.WARNING,
    message = "Use KotlinModule.Builder instead of named constructor parameters.",
    replaceWith = ReplaceWith(
        """KotlinModule.Builder()
            .withReflectionCacheSize(reflectionCacheSize)
            .configure(KotlinFeature.NullToEmptyCollection, nullToEmptyCollection)
            .configure(KotlinFeature.NullToEmptyMap, nullToEmptyMap)
            .configure(KotlinFeature.NullIsSameAsDefault, nullIsSameAsDefault)
            .configure(KotlinFeature.SingletonSupport, singletonSupport)
            .configure(KotlinFeature.StrictNullChecks, strictNullChecks)
            .build()""",
        "com.fasterxml.jackson.module.kotlin.KotlinFeature"
    )
) constructor(
    val reflectionCacheSize: Int = 512,
    val nullToEmptyCollection: Boolean = false,
    val nullToEmptyMap: Boolean = false,
    val nullIsSameAsDefault: Boolean = false,
    val singletonSupport: SingletonSupport = DISABLED,
    val strictNullChecks: Boolean = false
) : SimpleModule(KotlinModule::class.java.name, PackageVersion.VERSION) {
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "For ABI compatibility")
    constructor(
        reflectionCacheSize: Int,
        nullToEmptyCollection: Boolean,
        nullToEmptyMap: Boolean
    ) : this(
        Builder()
            .withReflectionCacheSize(reflectionCacheSize)
            .configure(NullToEmptyCollection, nullToEmptyCollection)
            .configure(NullToEmptyMap, nullToEmptyMap)
            .disable(NullIsSameAsDefault)
    )

    @Deprecated(level = DeprecationLevel.HIDDEN, message = "For ABI compatibility")
    constructor(
        reflectionCacheSize: Int,
        nullToEmptyCollection: Boolean,
        nullToEmptyMap: Boolean,
        nullIsSameAsDefault: Boolean
    ) : this(
        Builder()
            .withReflectionCacheSize(reflectionCacheSize)
            .configure(NullToEmptyCollection, nullToEmptyCollection)
            .configure(NullToEmptyMap, nullToEmptyMap)
            .configure(NullIsSameAsDefault, nullIsSameAsDefault)
    )

    @Suppress("DEPRECATION")
    private constructor(builder: Builder) : this(
        builder.reflectionCacheSize,
        builder.isEnabled(NullToEmptyCollection),
        builder.isEnabled(NullToEmptyMap),
        builder.isEnabled(NullIsSameAsDefault),
        when {
            builder.isEnabled(KotlinFeature.SingletonSupport) -> CANONICALIZE
            else -> DISABLED
        },
        builder.isEnabled(StrictNullChecks)
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

        when (singletonSupport) {
            DISABLED -> Unit
            CANONICALIZE -> {
 	        // [module-kotlin#225]: keep Kotlin singletons as singletons
                context.addDeserializerModifier(KotlinValueDeserializerModifier)
            }
        }

        context.insertAnnotationIntrospector(KotlinAnnotationIntrospector(context, cache, nullToEmptyCollection, nullToEmptyMap, nullIsSameAsDefault))
        context.appendAnnotationIntrospector(KotlinNamesAnnotationIntrospector(this, cache, ignoredClassesForImplyingJsonCreator))

        context.addDeserializers(KotlinDeserializers())
        context.addKeyDeserializers(KotlinKeyDeserializers)
        context.addSerializers(KotlinSerializers())
        context.addKeySerializers(KotlinKeySerializers())

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

        private val bitSet: BitSet = KotlinFeature.defaults

        fun withReflectionCacheSize(reflectionCacheSize: Int): Builder = apply {
            this.reflectionCacheSize = reflectionCacheSize
        }

        fun enable(feature: KotlinFeature): Builder = apply {
            bitSet.or(feature.bitSet)
        }

        fun disable(feature: KotlinFeature): Builder = apply {
            bitSet.andNot(feature.bitSet)
        }

        fun configure(feature: KotlinFeature, enabled: Boolean): Builder =
            when {
                enabled -> enable(feature)
                else -> disable(feature)
            }

        fun isEnabled(feature: KotlinFeature): Boolean =
            bitSet.intersects(feature.bitSet)

        @Deprecated(
            message = "Deprecated, use withReflectionCacheSize(reflectionCacheSize) instead.",
            replaceWith = ReplaceWith("withReflectionCacheSize(reflectionCacheSize)")
        )
        fun reflectionCacheSize(reflectionCacheSize: Int): Builder =
            withReflectionCacheSize(reflectionCacheSize)

        @Deprecated(
            message = "Deprecated, use isEnabled(NullToEmptyCollection) instead.",
            replaceWith = ReplaceWith(
                "isEnabled(KotlinFeature.NullToEmptyCollection)",
                "com.fasterxml.jackson.module.kotlin.KotlinFeature"
            )
        )
        fun getNullToEmptyCollection(): Boolean =
            isEnabled(NullToEmptyCollection)

        @Deprecated(
            message = "Deprecated, use configure(NullToEmptyCollection, enabled) instead.",
            replaceWith = ReplaceWith(
                "configure(KotlinFeature.NullToEmptyCollection, nullToEmptyCollection)",
                "com.fasterxml.jackson.module.kotlin.KotlinFeature"
            )
        )
        fun nullToEmptyCollection(nullToEmptyCollection: Boolean): Builder =
            configure(NullToEmptyCollection, nullToEmptyCollection)

        @Deprecated(
            message = "Deprecated, use isEnabled(NullToEmptyMap) instead.",
            replaceWith = ReplaceWith(
                "isEnabled(KotlinFeature.NullToEmptyMap)",
                "com.fasterxml.jackson.module.kotlin.KotlinFeature"
            )
        )
        fun getNullToEmptyMap(): Boolean =
            isEnabled(NullToEmptyMap)

        @Deprecated(
            message = "Deprecated, use configure(NullToEmptyMap, enabled) instead.",
            replaceWith = ReplaceWith(
                "configure(KotlinFeature.NullToEmptyMap, nullToEmptyMap)",
                "com.fasterxml.jackson.module.kotlin.KotlinFeature"
            )
        )
        fun nullToEmptyMap(nullToEmptyMap: Boolean): Builder =
            configure(NullToEmptyMap, nullToEmptyMap)

        @Deprecated(
            message = "Deprecated, use isEnabled(NullIsSameAsDefault) instead.",
            replaceWith = ReplaceWith(
                "isEnabled(KotlinFeature.NullIsSameAsDefault)",
                "com.fasterxml.jackson.module.kotlin.KotlinFeature"
            )
        )
        fun getNullIsSameAsDefault(): Boolean =
            isEnabled(NullIsSameAsDefault)

        @Deprecated(
            message = "Deprecated, use configure(NullIsSameAsDefault, enabled) instead.",
            replaceWith = ReplaceWith(
                "configure(KotlinFeature.NullIsSameAsDefault, nullIsSameAsDefault)",
                "com.fasterxml.jackson.module.kotlin.KotlinFeature"
            )
        )
        fun nullIsSameAsDefault(nullIsSameAsDefault: Boolean): Builder =
            configure(NullIsSameAsDefault, nullIsSameAsDefault)

        @Deprecated(
            message = "Deprecated, use isEnabled(SingletonSupport) instead.",
            replaceWith = ReplaceWith(
                "isEnabled(KotlinFeature.SingletonSupport)",
                "com.fasterxml.jackson.module.kotlin.KotlinFeature"
            )
        )
        fun getSingletonSupport(): SingletonSupport =
            when {
                isEnabled(KotlinFeature.SingletonSupport) -> CANONICALIZE
                else -> DISABLED
            }

        @Deprecated(
            message = "Deprecated, use configure(SingletonSupport, enabled) instead.",
            replaceWith = ReplaceWith(
                "configure(KotlinFeature.SingletonSupport, singletonSupport)",
                "com.fasterxml.jackson.module.kotlin.KotlinFeature"
            )
        )
        fun singletonSupport(singletonSupport: SingletonSupport): Builder =
            when (singletonSupport) {
                CANONICALIZE -> enable(KotlinFeature.SingletonSupport)
                else -> disable(KotlinFeature.SingletonSupport)
            }

        @Deprecated(
            message = "Deprecated, use isEnabled(StrictNullChecks) instead.",
            replaceWith = ReplaceWith(
                "isEnabled(KotlinFeature.StrictNullChecks)",
                "com.fasterxml.jackson.module.kotlin.KotlinFeature"
            )
        )
        fun getStrictNullChecks(): Boolean =
            isEnabled(StrictNullChecks)

        @Deprecated(
            message = "Deprecated, use configure(StrictNullChecks, enabled) instead.",
            replaceWith = ReplaceWith(
                "configure(KotlinFeature.StrictNullChecks, strictNullChecks)",
                "com.fasterxml.jackson.module.kotlin.KotlinFeature"
            )
        )
        fun strictNullChecks(strictNullChecks: Boolean): Builder =
            configure(StrictNullChecks, strictNullChecks)

        fun build(): KotlinModule =
            KotlinModule(this)
    }
}
