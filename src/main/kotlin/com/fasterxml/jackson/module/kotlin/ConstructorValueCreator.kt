package com.fasterxml.jackson.module.kotlin

import kotlin.reflect.KFunction
import kotlin.reflect.jvm.isAccessible

internal class ConstructorValueCreator<T>(override val callable: KFunction<T>) : ValueCreator<T>() {
    override val accessible: Boolean = callable.isAccessible

    init {
        if (!accessible) callable.isAccessible = true
    }
}
