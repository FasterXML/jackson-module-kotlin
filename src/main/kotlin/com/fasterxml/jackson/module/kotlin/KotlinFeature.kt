package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.databind.exc.InvalidNullException
import java.util.BitSet

/**
 * @see KotlinModule.Builder
 */
enum class KotlinFeature(internal val enabledByDefault: Boolean) {
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
     */
    SingletonSupport(enabledByDefault = false),

    /**
     * This feature represents whether to check deserialized collections.
     *
     * With this disabled, the default, collections which are typed to disallow null members (e.g. `List<String>`)
     * may contain null values after deserialization.
     * Enabling it protects against this but has significant performance impact.
     */
    @Deprecated(
        level = DeprecationLevel.ERROR,
        message = "This option will be migrated to the new backend in 2.21.",
        replaceWith = ReplaceWith("NewStrictNullChecks")
    )
    StrictNullChecks(enabledByDefault = false),

    /**
     * By enabling this feature, the property name on Kotlin is used as the implicit name for the getter.
     *
     * By default, the getter name is used during serialization.
     * This name may be different from the parameter/field name, in which case serialization results
     * may be incorrect or annotations may malfunction.
     * See [jackson-module-kotlin#630] for details.
     *
     * By enabling this feature, such malfunctions will not occur.
     *
     * On the other hand, enabling this option increases the amount of reflection processing,
     * which may result in performance degradation for both serialization and deserialization.
     * In addition, the adjustment of behavior using get:JvmName is disabled.
     * Note also that this feature does not apply to setters.
     */
    KotlinPropertyNameAsImplicitName(enabledByDefault = false),

    /**
     * This feature represents whether to handle [kotlin.time.Duration] using [java.time.Duration] as conversion bridge.
     *
     * This allows use Kotlin Duration type with [com.fasterxml.jackson.datatype.jsr310.JavaTimeModule].
     * `@JsonFormat` annotations need to be declared either on getter using `@get:JsonFormat` or field using `@field:JsonFormat`.
     * See [jackson-module-kotlin#651] for details.
     */
    UseJavaDurationConversion(enabledByDefault = false),

    /**
     * New [StrictNullChecks] feature with improved throughput.
     * Internally, it will be the same as if [JsonSetter] (contentNulls = FAIL) had been granted.
     * Benchmarks show that it can check for illegal nulls with throughput nearly identical to the default (see [jackson-module-kotlin#719]).
     *
     * Note that in the new backend, the exception thrown has changed from [MissingKotlinParameterException] to [InvalidNullException].
     * The message will be changed accordingly.
     * Since 2.19, the base class of [MissingKotlinParameterException] has also been changed to [InvalidNullException],
     * so be careful when catching it.
     *
     * This is a temporary option for a phased backend migration,
     * which will eventually be merged into [StrictNullChecks].
     * Also, specifying both this and [StrictNullChecks] is not permitted.
     */
    NewStrictNullChecks(enabledByDefault = false);

    internal val bitSet: BitSet = (1 shl ordinal).toBitSet()

    companion object {
        internal val defaults
            get() = entries.fold(BitSet(Int.SIZE_BITS)) { acc, cur ->
                acc.apply { if (cur.enabledByDefault) this.or(cur.bitSet) }
            }
    }
}
