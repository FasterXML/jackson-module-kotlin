package tools.jackson.module.kotlin

import java.util.BitSet
import kotlin.math.pow

/**
 * @see KotlinModule.Builder
 */
enum class KotlinFeature(private val enabledByDefault: Boolean) {
    /**
     * This feature represents whether to deserialize `null` values for collection properties as empty collections.
     */
    NullToEmptyCollection(enabledByDefault = false),

    /**
     * This feature represents whether to deserialize `null` values for a map property to an empty map object.
     */
    NullToEmptyMap(enabledByDefault = false),

    /**
     * This feature represents whether to treat `null` values as absent when deserializing,
     * thereby using the default value provided in Kotlin.
     */
    NullIsSameAsDefault(enabledByDefault = false),

    /**
     * By default, there's no special handling of singletons (pre-2.10 behavior).
     * Each time a Singleton object is deserialized a new instance is created.
     *
     * When this feature is enabled, it will deserialize then canonicalize (was the default in 2.10).
     * Deserializing a singleton overwrites the value of the single instance.
     *
     * See [jackson-module-kotlin#225]: keep Kotlin singletons as singletons.
     * @see tools.jackson.module.kotlin.SingletonSupport
     */
    SingletonSupport(enabledByDefault = false),

    /**
     * This feature represents whether to check deserialized collections.
     *
     * With this disabled, the default, collections which are typed to disallow null members (e.g. `List<String>`)
     * may contain null values after deserialization.
     * Enabling it protects against this but has significant performance impact.
     */
    StrictNullChecks(enabledByDefault = false);

    internal val bitSet: BitSet = 2.0.pow(ordinal).toInt().toBitSet()

    companion object {
        internal val defaults
            get() = 0.toBitSet().apply {
                values().filter { it.enabledByDefault }.forEach { or(it.bitSet) }
            }
    }
}
