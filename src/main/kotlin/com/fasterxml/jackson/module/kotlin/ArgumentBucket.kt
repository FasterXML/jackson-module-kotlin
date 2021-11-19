package com.fasterxml.jackson.module.kotlin

import kotlin.reflect.KParameter

internal class BucketGenerator(parameters: List<KParameter>) {
    private val paramSize: Int = parameters.size
    val maskSize = (paramSize / Int.SIZE_BITS) + 1
    // For Optional and Primitive types, set the initial value because the function cannot be called if the argument is null.
    private val originalValues: Array<Any?> = Array(paramSize) {
        val param = parameters[it]

        if (param.isOptional) {
            ABSENT_VALUE[param.type.erasedType()]
        } else {
            null
        }
    }
    private val originalMasks: IntArray = IntArray(maskSize) { FILLED_MASK }

    fun generate() = ArgumentBucket(paramSize, originalValues.clone(), originalMasks.clone())

    companion object {
        private const val FILLED_MASK = -1

        private val ABSENT_VALUE: Map<Class<*>, Any> = mapOf(
            Boolean::class.javaPrimitiveType!! to false,
            Char::class.javaPrimitiveType!! to Char.MIN_VALUE,
            Byte::class.javaPrimitiveType!! to Byte.MIN_VALUE,
            Short::class.javaPrimitiveType!! to Short.MIN_VALUE,
            Int::class.javaPrimitiveType!! to Int.MIN_VALUE,
            Long::class.javaPrimitiveType!! to Long.MIN_VALUE,
            Float::class.javaPrimitiveType!! to Float.MIN_VALUE,
            Double::class.javaPrimitiveType!! to Double.MIN_VALUE
        )
    }
}

/**
 * Class for managing arguments and their initialization state.
 * [masks] is used to manage the initialization state of arguments, and is also a mask to indicate whether to use default arguments in Kotlin.
 * For the [masks] bit, 0 means initialized and 1 means uninitialized.
 *
 * @property  values  Arguments arranged in order in the manner of a bucket sort.
 */
internal class ArgumentBucket(
    private val paramSize: Int,
    val values: Array<Any?>,
    private val masks: IntArray
) {
    private var initializedCount: Int = 0

    private fun getMaskAddress(index: Int): Pair<Int, Int> = (index / Int.SIZE_BITS) to (index % Int.SIZE_BITS)

    /**
     * Set the argument. The second and subsequent inputs for the same `index` will be ignored.
     */
    operator fun set(index: Int, value: Any?) {
        val maskAddress = getMaskAddress(index)

        val updatedMask = masks[maskAddress.first] and BIT_FLAGS[maskAddress.second]

        if (updatedMask != masks[maskAddress.first]) {
            values[index] = value
            masks[maskAddress.first] = updatedMask
            initializedCount++
        }
    }

    fun isFullInitialized(): Boolean = initializedCount == paramSize

    /**
     * An array of values to be used when making calls with default arguments.
     * The null at the end is a marker for synthetic method.
     * @return arrayOf(*values, *masks, null)
     */
    fun getValuesOnDefault(): Array<Any?> = values.copyOf(values.size + masks.size + 1).apply {
        masks.forEachIndexed { i, mask ->
            this[values.size + i] = mask
        }
    }

    companion object {
        // List of Int with only 1 bit enabled.
        private val BIT_FLAGS: List<Int> = IntArray(Int.SIZE_BITS) { (1 shl it).inv() }.asList()
    }
}
