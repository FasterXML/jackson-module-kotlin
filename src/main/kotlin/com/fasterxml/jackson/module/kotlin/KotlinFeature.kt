package com.fasterxml.jackson.module.kotlin

import java.util.BitSet
import kotlin.math.pow

enum class KotlinFeature(val enabledByDefault: Boolean) {
    NullToEmptyCollection(enabledByDefault = false),
    NullToEmptyMap(enabledByDefault = false),
    NullIsSameAsDefault(enabledByDefault = false),
    SingletonSupport(enabledByDefault = false),
    StrictNullChecks(enabledByDefault = false);

    internal val bitSet: BitSet = 2.0.pow(ordinal).toInt().toBitSet()
}
