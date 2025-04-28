package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullIsSameAsDefault
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyCollection
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyMap
import com.fasterxml.jackson.module.kotlin.KotlinFeature.SingletonSupport
import com.fasterxml.jackson.module.kotlin.KotlinFeature.StrictNullChecks
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NewStrictNullChecks
import com.fasterxml.jackson.module.kotlin.KotlinFeature.KotlinPropertyNameAsImplicitName
import com.fasterxml.jackson.module.kotlin.KotlinFeature.UseJavaDurationConversion
import java.util.*

fun Class<*>.isKotlinClass(): Boolean = this.isAnnotationPresent(Metadata::class.java)

/**
 * @constructor To avoid binary compatibility issues, the primary constructor is not published.
 *  Please use KotlinModule.Builder or extensions that use it.
 * @property reflectionCacheSize     Default: 512.  Size, in items, of the caches used for mapping objects.
 * @property nullToEmptyCollection   Default: false.  Whether to deserialize null values for collection properties as
 *  empty collections.
 * @property nullToEmptyMap          Default: false.  Whether to deserialize null values for a map property to an empty
 *  map object.
 * @property nullIsSameAsDefault     Default false.  Whether to treat null values as absent when deserializing, thereby
 *  using the default value provided in Kotlin.
 * @property singletonSupport        Default: false.  Mode for singleton handling.
 *  See [KotlinFeature.SingletonSupport]
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
class KotlinModule private constructor(
    val reflectionCacheSize: Int = Builder.DEFAULT_CACHE_SIZE,
    val nullToEmptyCollection: Boolean = NullToEmptyCollection.enabledByDefault,
    val nullToEmptyMap: Boolean = NullToEmptyMap.enabledByDefault,
    val nullIsSameAsDefault: Boolean = NullIsSameAsDefault.enabledByDefault,
    val singletonSupport: Boolean = SingletonSupport.enabledByDefault,
    @Suppress("DEPRECATION_ERROR")
    strictNullChecks: Boolean = StrictNullChecks.enabledByDefault,
    val kotlinPropertyNameAsImplicitName: Boolean = KotlinPropertyNameAsImplicitName.enabledByDefault,
    val useJavaDurationConversion: Boolean = UseJavaDurationConversion.enabledByDefault,
    private val newStrictNullChecks: Boolean = NewStrictNullChecks.enabledByDefault,
) : SimpleModule(KotlinModule::class.java.name, PackageVersion.VERSION) {
    /*
     * Prior to 2.18, an older Enum called SingletonSupport was used to manage feature.
     * To deprecate it and replace it with singletonSupport: Boolean, the following steps are in progress.
     *
     * 1. add enabledSingletonSupport: Boolean property
     * 2. delete SingletonSupport class and change the property to singletonSupport: Boolean
     * 3. remove the enabledSingletonSupport property
     *
     * Now that 2 is complete, deprecation is in progress for 3.
     */
    @Deprecated(
        level = DeprecationLevel.ERROR,
        message = "This property is scheduled to be removed in 2.21 or later" +
                " in order to unify the use of KotlinFeature.",
        replaceWith = ReplaceWith("singletonSupport")
    )
    val enabledSingletonSupport: Boolean get() = singletonSupport

    private val oldStrictNullChecks: Boolean = strictNullChecks

    // To reduce the amount of destructive changes, no properties will be added to the public.
    val strictNullChecks: Boolean = if (strictNullChecks) {
        if (newStrictNullChecks) {
            throw IllegalArgumentException("Enabling both StrictNullChecks and NewStrictNullChecks is not permitted.")
        }

        true
    } else {
        newStrictNullChecks
    }

    companion object {
        // Increment when option is added
        private const val serialVersionUID = 3L
    }

    @Deprecated(
        level = DeprecationLevel.HIDDEN,
        message = "If you have no choice but to initialize KotlinModule from reflection, use this constructor."
    )
    constructor() : this()

    private constructor(builder: Builder) : this(
        builder.reflectionCacheSize,
        builder.isEnabled(NullToEmptyCollection),
        builder.isEnabled(NullToEmptyMap),
        builder.isEnabled(NullIsSameAsDefault),
        builder.isEnabled(SingletonSupport),
        @Suppress("DEPRECATION_ERROR")
        builder.isEnabled(StrictNullChecks),
        builder.isEnabled(KotlinPropertyNameAsImplicitName),
        builder.isEnabled(UseJavaDurationConversion),
        builder.isEnabled(NewStrictNullChecks),
    )

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        if (!context.isEnabled(MapperFeature.USE_ANNOTATIONS)) {
            throw IllegalStateException("The Jackson Kotlin module requires USE_ANNOTATIONS to be true or it cannot function")
        }

        val cache = ReflectionCache(reflectionCacheSize)

        context.addValueInstantiators(KotlinInstantiators(cache, nullToEmptyCollection, nullToEmptyMap, nullIsSameAsDefault, oldStrictNullChecks))

        if (singletonSupport) {
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
            KotlinNamesAnnotationIntrospector(cache, newStrictNullChecks, kotlinPropertyNameAsImplicitName)
        )

        context.addDeserializers(KotlinDeserializers(cache, useJavaDurationConversion))
        context.addKeyDeserializers(KotlinKeyDeserializers(cache))
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
