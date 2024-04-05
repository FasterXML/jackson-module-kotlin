package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullIsSameAsDefault
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyCollection
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyMap
import com.fasterxml.jackson.module.kotlin.KotlinFeature.StrictNullChecks
import com.fasterxml.jackson.module.kotlin.KotlinFeature.KotlinPropertyNameAsImplicitName
import com.fasterxml.jackson.module.kotlin.KotlinFeature.UseJavaDurationConversion
import com.fasterxml.jackson.module.kotlin.SingletonSupport.CANONICALIZE
import com.fasterxml.jackson.module.kotlin.SingletonSupport.DISABLED
import java.util.*
import kotlin.reflect.KClass

fun Class<*>.isKotlinClass(): Boolean = this.isAnnotationPresent(Metadata::class.java)

/**
 * @property reflectionCacheSize     Default: 512.  Size, in items, of the caches used for mapping objects.
 * @property nullToEmptyCollection   Default: false.  Whether to deserialize null values for collection properties as
 *  empty collections.
 * @property nullToEmptyMap          Default: false.  Whether to deserialize null values for a map property to an empty
 *  map object.
 * @property nullIsSameAsDefault     Default false.  Whether to treat null values as absent when deserializing, thereby
 *  using the default value provided in Kotlin.
 * @property singletonSupport        Default: DISABLED.  Mode for singleton handling.
 *  See {@link com.fasterxml.jackson.module.kotlin.SingletonSupport label}
 * @property enabledSingletonSupport Default: false.  A temporary property that is maintained until the return value of `singletonSupport` is changed.
 *  It will be removed in 2.21.
 * @property strictNullChecks        Default: false.  Whether to check deserialized collections.  With this disabled,
 *  the default, collections which are typed to disallow null members
 *  (e.g. List<String>) may contain null values after deserialization.  Enabling it
 *  protects against this but has significant performance impact.
 * @property kotlinPropertyNameAsImplicitName Default: false.  Whether to use the Kotlin property name as the implicit name.
 *  See [KotlinFeature.KotlinPropertyNameAsImplicitName] for details.
 * @property useJavaDurationConversion Default: false.  Whether to use [java.time.Duration] as a bridge for [kotlin.time.Duration].
 *  This allows use Kotlin Duration type with [com.fasterxml.jackson.datatype.jsr310.JavaTimeModule].
 */
class KotlinModule @Deprecated(
    level = DeprecationLevel.ERROR,
    message = "Use KotlinModule.Builder instead of named constructor parameters. It will be HIDDEN at 2.18.",
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
    val reflectionCacheSize: Int = Builder.DEFAULT_CACHE_SIZE,
    val nullToEmptyCollection: Boolean = NullToEmptyCollection.enabledByDefault,
    val nullToEmptyMap: Boolean = NullToEmptyMap.enabledByDefault,
    val nullIsSameAsDefault: Boolean = NullIsSameAsDefault.enabledByDefault,
    @property:Deprecated(
        level = DeprecationLevel.WARNING,
        message = "The return value will be Boolean in 2.19. Until then, use enabledSingletonSupport.",
        replaceWith = ReplaceWith("enabledSingletonSupport")
    )
    val singletonSupport: SingletonSupport = DISABLED,
    val strictNullChecks: Boolean = StrictNullChecks.enabledByDefault,
    @Deprecated(
        level = DeprecationLevel.WARNING,
        message = "There was a discrepancy between the property name and the Feature name." +
            " To migrate to the correct property name, it will be ERROR in 2.18 and removed in 2.19.",
        replaceWith = ReplaceWith("kotlinPropertyNameAsImplicitName")
    )
    val useKotlinPropertyNameForGetter: Boolean = KotlinPropertyNameAsImplicitName.enabledByDefault,
    val useJavaDurationConversion: Boolean = UseJavaDurationConversion.enabledByDefault,
) : SimpleModule(KotlinModule::class.java.name, PackageVersion.VERSION) {
    val kotlinPropertyNameAsImplicitName: Boolean get() = useKotlinPropertyNameForGetter
    val enabledSingletonSupport: Boolean get() = singletonSupport == CANONICALIZE

    companion object {
        // Increment when option is added
        private const val serialVersionUID = 2L
    }

    @Deprecated(
        level = DeprecationLevel.HIDDEN,
        message = "If you have no choice but to initialize KotlinModule from reflection, use this constructor."
    )
    @Suppress("DEPRECATION_ERROR")
    constructor() : this()

    @Suppress("DEPRECATION_ERROR")
    private constructor(builder: Builder) : this(
        builder.reflectionCacheSize,
        builder.isEnabled(NullToEmptyCollection),
        builder.isEnabled(NullToEmptyMap),
        builder.isEnabled(NullIsSameAsDefault),
        when {
            builder.isEnabled(KotlinFeature.SingletonSupport) -> CANONICALIZE
            else -> DISABLED
        },
        builder.isEnabled(StrictNullChecks),
        builder.isEnabled(KotlinPropertyNameAsImplicitName),
        builder.isEnabled(UseJavaDurationConversion),
    )

    private val ignoredClassesForImplyingJsonCreator = emptySet<KClass<*>>()

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        if (!context.isEnabled(MapperFeature.USE_ANNOTATIONS)) {
            throw IllegalStateException("The Jackson Kotlin module requires USE_ANNOTATIONS to be true or it cannot function")
        }

        val cache = ReflectionCache(reflectionCacheSize)

        context.addValueInstantiators(KotlinInstantiators(cache, nullToEmptyCollection, nullToEmptyMap, nullIsSameAsDefault, strictNullChecks))

        if (enabledSingletonSupport) {
            context.addBeanDeserializerModifier(KotlinBeanDeserializerModifier)
        }

        context.insertAnnotationIntrospector(KotlinAnnotationIntrospector(
            context,
            cache,
            nullToEmptyCollection,
            nullToEmptyMap,
            nullIsSameAsDefault,
            useJavaDurationConversion
        ))
        context.appendAnnotationIntrospector(
            KotlinNamesAnnotationIntrospector(
                cache,
                ignoredClassesForImplyingJsonCreator,
                useKotlinPropertyNameForGetter)
        )

        context.addDeserializers(KotlinDeserializers(cache, useJavaDurationConversion))
        context.addKeyDeserializers(KotlinKeyDeserializers)
        context.addSerializers(KotlinSerializers())
        context.addKeySerializers(KotlinKeySerializers())

        // ranges
        context.setMixInAnnotations(ClosedRange::class.java, ClosedRangeMixin::class.java)
    }

    class Builder {
        companion object {
            internal const val DEFAULT_CACHE_SIZE = 512
        }

        var reflectionCacheSize: Int = DEFAULT_CACHE_SIZE
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

        fun build(): KotlinModule =
            KotlinModule(this)
    }
}
