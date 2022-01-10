package com.fasterxml.jackson.module.kotlin

import kotlin.reflect.KParameter

/**
 * Calculation of where the initialization state of KParameter.index is recorded in the masks.
 * @return index / 32(= Int.SIZE_BITS)
 */
private fun getMaskIndex(index: Int) = index shr 5

/**
 * Calculation of where the initialization state of KParameter.index is recorded in the bit of int.
 * @return index % 32(= Int.SIZE_BITS)
 */
private fun getFlagIndex(index: Int) = index and 31

/**
 * Generator for [ArgumentBucket].
 * Refer to the documentation of [ArgumentBucket] and factory function for the contents of each argument.
 */
internal class BucketGenerator private constructor(
    private val paramSize: Int,
    private val originalValues: Array<Any?>,
    private val originalMasks: IntArray,
    private val originalInitializedCount: Int,
    private val parameters: List<KParameter>
) {
    fun generate(): ArgumentBucket = ArgumentBucket(
        paramSize,
        originalValues.clone(),
        originalMasks.clone(),
        originalInitializedCount,
        parameters
    )

    companion object {
        // -1 is a value where all bits are filled with 1
        private const val FILLED_MASK: Int = -1

        // The maximum size of the array is obtained by getMaskIndex(paramSize) + 1.
        private fun getOriginalMasks(paramSize: Int): IntArray = IntArray(getMaskIndex(paramSize) + 1) { FILLED_MASK }

        /**
         * @return [BucketGenerator] when the target of the call is a constructor.
         */
        fun forConstructor(parameters: List<KParameter>): BucketGenerator {
            val paramSize = parameters.size
            // Since the constructor does not require any instance parameters, do not operation the values.
            return BucketGenerator(paramSize, Array(paramSize) { null }, getOriginalMasks(paramSize), 0, parameters)
        }

        /**
         * @return [BucketGenerator] when the target of the call is a method.
         */
        fun forMethod(parameters: List<KParameter>, instance: Any): BucketGenerator {
            val paramSize = parameters.size

            // Since the method requires instance parameter, it is necessary to perform several operations.

            // In the jackson-module-kotlin process, instance parameters are always at the top,
            // so they should be placed at the top of originalValues.
            val originalValues = Array<Any?>(paramSize) { null }.apply { this[0] = instance }
            // Since the instance parameters have already been initialized,
            // the originalMasks must also be in the corresponding state.
            val originalMasks = getOriginalMasks(paramSize).apply { this[0] = this[0] and 1.inv() }
            // Since the instance parameters have already been initialized, the originalInitializedCount will be 1.
            return BucketGenerator(paramSize, originalValues, originalMasks, 1, parameters)
        }
    }
}

/**
 * Class for managing arguments and their initialization state.
 * [masks] is used to manage the initialization state of arguments.
 * For the [masks] bit, 0 means initialized and 1 means uninitialized.
 *
 * At this point, this management method may not necessarily be ideal,
 * but the reason that using this method is to simplify changes like @see <a href="https://github.com/FasterXML/jackson-module-kotlin/pull/439">#439</a>.
 *
 * @property paramSize Cache of [parameters].size.
 * @property actualValues Arguments arranged in order in the manner of a bucket sort.
 * @property masks Initialization state of arguments.
 * @property initializedCount Number of initialized parameters.
 * @property parameters Parameters of the KFunction to be called.
 */
internal class ArgumentBucket(
    private val paramSize: Int,
    val actualValues: Array<Any?>,
    private val masks: IntArray,
    private var initializedCount: Int,
    private val parameters: List<KParameter>
): Map<KParameter, Any?> {
    class Entry internal constructor(
        override val key: KParameter,
        override var value: Any?
    ) : Map.Entry<KParameter, Any?>

    /**
     * If the argument corresponding to KParameter.index is initialized, true is returned.
     */
    private fun isInitialized(index: Int): Boolean = masks[getMaskIndex(index)]
        .let { (it and BIT_FLAGS[getFlagIndex(index)]) == it }

    override val entries: Set<Map.Entry<KParameter, Any?>>
        get() = parameters.fold(HashSet()) { acc, cur ->
            val index = cur.index
            acc.apply { if (isInitialized(index)) add(Entry(parameters[index], actualValues[index])) }
        }
    override val keys: Set<KParameter>
        get() = parameters.fold(HashSet()) { acc, cur -> acc.apply { if (isInitialized(cur.index)) add(cur) } }
    override val size: Int
        get() = initializedCount
    override val values: Collection<Any?>
        get() = values.filterIndexed { index, _ -> isInitialized(index) }

    override fun containsKey(key: KParameter): Boolean = isInitialized(key.index)

    override fun containsValue(value: Any?): Boolean =
        (0 until paramSize).any { isInitialized(it) && value == actualValues[it] }

    override fun get(key: KParameter): Any? = actualValues[key.index]

    override fun isEmpty(): Boolean = initializedCount == 0

    /**
     * Set the value to KParameter.index.
     * However, if the corresponding index has already been initialized, nothing is done.
     */
    operator fun set(index: Int, value: Any?) {
        val maskIndex = getMaskIndex(index)
        val flagIndex = getFlagIndex(index)

        val updatedMask = masks[maskIndex] and BIT_FLAGS[flagIndex]

        if (updatedMask != masks[maskIndex]) {
            actualValues[index] = value
            masks[maskIndex] = updatedMask
            initializedCount++
        }
    }

    /**
     * Return true if all arguments are [set].
     */
    fun isFullInitialized(): Boolean = initializedCount == paramSize

    companion object {
        // List of Int with only 1 bit enabled.
        private val BIT_FLAGS: List<Int> = IntArray(Int.SIZE_BITS) { (1 shl it).inv() }.asList()
    }
}
