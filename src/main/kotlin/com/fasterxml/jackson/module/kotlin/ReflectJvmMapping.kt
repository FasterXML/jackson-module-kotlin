package com.fasterxml.jackson.module.kotlin

import java.lang.reflect.Constructor
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaConstructor

val <T : Any> Constructor<T>.kotlinCtor: KFunction<T>?
    get() {
        val kotlinClass = declaringClass.kotlin
        return if (kotlinClass.isValue) {
            val parameterTypes = this.parameters.map { p -> p.type }
            kotlinClass.constructors.firstOrNull { it.parameters.map { p -> p.type.erasedType() } == parameterTypes }
        } else {
            kotlinClass.constructors.firstOrNull { it.javaConstructor == this }
        }
    }
