package com.fasterxml.jackson.module.kotlin

import kotlin.reflect.KParameter

internal class BucketGenerator private constructor(paramSize: Int, instanceParameter: KParameter?, instance: Any?) {
    private val originalParameters = arrayOfNulls<KParameter>(paramSize)
    private val originalArguments = arrayOfNulls<Any?>(paramSize)
    private val initialCount: Int

    init {
        if (instance != null) {
            originalParameters[0] = instanceParameter
            originalArguments[0] = instance
            initialCount = 1
        } else {
            initialCount = 0
        }
    }

    fun generate(): ArgumentBucket = ArgumentBucket(
        parameters = originalParameters.clone(),
        arguments = originalArguments.clone(),
        count = initialCount
    )

    companion object {
        fun forConstructor(paramSize: Int): BucketGenerator = BucketGenerator(paramSize, null, null)

        fun forMethod(paramSize: Int, instanceParameter: KParameter, instance: Any): BucketGenerator =
            BucketGenerator(paramSize, instanceParameter, instance)
    }
}

internal class ArgumentBucket(
    private val parameters: Array<KParameter?>,
    val arguments: Array<Any?>,
    private var count: Int
) : Map<KParameter, Any?> {
    operator fun set(key: KParameter, value: Any?) {
        arguments[key.index] = value
        parameters[key.index] = key

        // Multiple calls are not checked because internally no calls are made more than once per argument.
        count++
    }

    val isFullInitialized: Boolean get() = count == arguments.size

    private class Entry(override val key: KParameter, override val value: Any?) : Map.Entry<KParameter, Any?>

    override val entries: Set<Map.Entry<KParameter, Any?>>
        get() = parameters.mapNotNull { key -> key?.let { Entry(it, arguments[it.index]) } }.toSet()
    override val keys: Set<KParameter>
        get() = parameters.filterNotNull().toSet()
    override val size: Int
        get() = count
    override val values: Collection<Any?>
        get() = keys.map { arguments[it.index] }

    override fun isEmpty(): Boolean = this.size == 0

    // Skip the check here, as it is only called after the check for containsKey.
    override fun get(key: KParameter): Any? = arguments[key.index]

    override fun containsValue(value: Any?): Boolean = keys.any { arguments[it.index] == value }

    override fun containsKey(key: KParameter): Boolean = parameters.any { it?.index == key.index }
}
