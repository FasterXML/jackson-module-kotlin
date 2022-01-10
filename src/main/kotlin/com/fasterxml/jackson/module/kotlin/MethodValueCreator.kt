package com.fasterxml.jackson.module.kotlin

import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.isAccessible

internal class MethodValueCreator<T> private constructor(
    override val callable: KFunction<T>,
    override val accessible: Boolean,
    companionObjectInstance: Any
) : ValueCreator<T>() {
    override val bucketGenerator: BucketGenerator =
        BucketGenerator.forMethod(callable.parameters, companionObjectInstance)

    companion object {
        fun <T> of(callable: KFunction<T>): MethodValueCreator<T>? {
            // we shouldn't have an instance or receiver parameter and if we do, just go with default Java-ish behavior
            if (callable.extensionReceiverParameter != null) return null

            val possibleCompanion = callable.instanceParameter!!.type.erasedType().kotlin

            // abort, we have some unknown case here
            if (!possibleCompanion.isCompanion) return null

            // To prevent the call from failing, save the initial value and then rewrite the flag.
            val initialCallableAccessible = callable.isAccessible
            if (!initialCallableAccessible) callable.isAccessible = true

            val (companionObjectInstance: Any, accessible: Boolean) = try {
                // throws ex
                val instance = possibleCompanion.objectInstance!!

                // If an instance of the companion object can be obtained, accessibility depends on the KFunction
                instance to initialCallableAccessible
            } catch (ex: IllegalAccessException) {
                // fallback for when an odd access exception happens through Kotlin reflection
                possibleCompanion.java.enclosingClass.declaredFields
                    .firstOrNull { it.type.kotlin.isCompanion }
                    ?.let {
                        it.isAccessible = true

                        // If the instance of the companion object cannot be obtained, accessibility will always be false
                        it.get(null) to false
                    } ?: throw ex
            }

            return MethodValueCreator(callable, accessible, companionObjectInstance)
        }
    }
}
