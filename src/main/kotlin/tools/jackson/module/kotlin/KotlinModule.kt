package tools.jackson.module.kotlin

import kotlin.reflect.KClass
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.module.SimpleModule
import tools.jackson.module.kotlin.KotlinFeature.*
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
 * @property enabledSingletonSupport        Default: DISABLED.  Mode for singleton handling.
 *  See {@link tools.jackson.module.kotlin.SingletonSupport label}
 * @property strictNullChecks        Default: false.  Whether to check deserialized collections.  With this disabled,
 *  the default, collections which are typed to disallow null members
 *  (e.g. List<String>) may contain null values after deserialization.  Enabling it
 *  protects against this but has significant performance impact.
 * @property kotlinPropertyNameAsImplicitName Default: false.  Whether to use the Kotlin property name as the implicit name.
 *  See [KotlinFeature.KotlinPropertyNameAsImplicitName] for details.
 * @property useJavaDurationConversion Default: false.  Whether to use [java.time.Duration] as a bridge for [kotlin.time.Duration].
 *  This allows use Kotlin Duration type with [tools.jackson.datatype.jsr310.JavaTimeModule].
 */
class KotlinModule private constructor(
    val reflectionCacheSize: Int = Builder.DEFAULT_CACHE_SIZE,
    val nullToEmptyCollection: Boolean = NullToEmptyCollection.enabledByDefault,
    val nullToEmptyMap: Boolean = NullToEmptyMap.enabledByDefault,
    val nullIsSameAsDefault: Boolean = NullIsSameAsDefault.enabledByDefault,
    val enabledSingletonSupport: Boolean = KotlinFeature.SingletonSupport.enabledByDefault,
    val strictNullChecks: Boolean = StrictNullChecks.enabledByDefault,
    val kotlinPropertyNameAsImplicitName: Boolean = KotlinPropertyNameAsImplicitName.enabledByDefault,
    val useJavaDurationConversion: Boolean = UseJavaDurationConversion.enabledByDefault,
) : SimpleModule(KotlinModule::class.java.name, PackageVersion.VERSION) {

    companion object {
        // Increment when option is added
        private const val serialVersionUID = 2L
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
        builder.isEnabled(StrictNullChecks),
        builder.isEnabled(KotlinPropertyNameAsImplicitName),
        builder.isEnabled(UseJavaDurationConversion),
    )

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        if (!context.isEnabled(MapperFeature.USE_ANNOTATIONS)) {
            throw IllegalStateException("The Jackson Kotlin module requires USE_ANNOTATIONS to be true or it cannot function")
        }

        val cache = ReflectionCache(reflectionCacheSize)

        context.addValueInstantiators(KotlinInstantiators(cache, nullToEmptyCollection, nullToEmptyMap, nullIsSameAsDefault, strictNullChecks))

        if (enabledSingletonSupport) {
            // [module-kotlin#225]: keep Kotlin singletons as singletons
            context.addDeserializerModifier(KotlinValueDeserializerModifier)
        }

        context.insertAnnotationIntrospector(KotlinAnnotationIntrospector(
            context,
            cache,
            nullToEmptyCollection,
            nullToEmptyMap,
            nullIsSameAsDefault,
            useJavaDurationConversion
        ))
        context.appendAnnotationIntrospector(KotlinNamesAnnotationIntrospector(cache, kotlinPropertyNameAsImplicitName))

        context.addDeserializers(KotlinDeserializers(cache, useJavaDurationConversion))
        context.addKeyDeserializers(KotlinKeyDeserializers)
        context.addSerializers(KotlinSerializers())
        context.addKeySerializers(KotlinKeySerializers())

        // ranges
        context.setMixIn(ClosedRange::class.java, ClosedRangeMixin::class.java)
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
