package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.util.JacksonFeature
import java.util.*

enum class KotlinFeature(private val enabledByDefault: Boolean, private val mask: Int) : JacksonFeature {

    NullToEmptyCollection(enabledByDefault = false, mask = 1),
    NullToEmptyMap(enabledByDefault = false, mask = 2),
    NullIsSameAsDefault(enabledByDefault = false, mask = 4),
    SingletonSupport(enabledByDefault = false, mask = 8),
    StrictNullChecks(enabledByDefault = false, mask = 16);

    val bitSet: BitSet = mask.toBitSet()

    override fun enabledByDefault() = enabledByDefault
    override fun getMask() = mask
    override fun enabledIn(flags: Int) = bitSet.intersects(flags.toBitSet())
}
