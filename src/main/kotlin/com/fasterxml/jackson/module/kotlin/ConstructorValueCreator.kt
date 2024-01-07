package com.fasterxml.jackson.module.kotlin

import kotlin.reflect.KFunction
import kotlin.reflect.jvm.isAccessible

internal class ConstructorValueCreator<T>(override val callable: KFunction<T>) : ValueCreator<T>() {
    override val accessible: Boolean = callable.isAccessible
    override val bucketGenerator: BucketGenerator = BucketGenerator.forConstructor(callable.parameters.size)

    init {
        // To prevent the call from failing, save the initial value and then rewrite the flag.
        if (!accessible) callable.isAccessible = true
    }
}
