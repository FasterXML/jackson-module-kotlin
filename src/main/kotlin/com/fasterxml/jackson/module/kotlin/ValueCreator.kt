package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.MapperFeature
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

/**
 * A class that abstracts the creation of instances by calling KFunction.
 * @see KotlinValueInstantiator
 */
internal sealed class ValueCreator<T> {
    /**
     * Function to be call.
     */
    protected abstract val callable: KFunction<T>

    /**
     * Initial value for accessibility by reflection.
     */
    protected abstract val accessible: Boolean

    /**
     * ValueParameters of the KFunction to be called.
     */
    val valueParameters: List<KParameter> by lazy { callable.valueParameters }

    /**
     * Checking process to see if access from context is possible.
     * @throws  IllegalAccessException
     */
    fun checkAccessibility(ctxt: DeserializationContext) {
        if ((!accessible && ctxt.config.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) ||
            (accessible && ctxt.config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS))) {
            return
        }

        throw IllegalAccessException("Cannot access to function or companion object instance, target: $callable")
    }

    /**
     * Function call with default values enabled.
     */
    fun callBy(args: Map<KParameter, Any?>): T = callable.callBy(args)
}

internal class MethodValueCreator<T> private constructor(
    override val callable: KFunction<T>,
    override val accessible: Boolean,
    val companionObjectInstance: Any
) : ValueCreator<T>() {
    val instanceParameter: KParameter = callable.instanceParameter!!

    companion object {
        fun <T> of(callable: KFunction<T>): MethodValueCreator<T>? {
            // we shouldn't have an instance or receiver parameter and if we do, just go with default Java-ish behavior
            if (callable.extensionReceiverParameter != null) return null

            val possibleCompanion = callable.instanceParameter!!.type.erasedType().kotlin

            // abort, we have some unknown case here
            if (!possibleCompanion.isCompanion) return null

            val (companionObjectInstance: Any, accessible: Boolean) = try {
                // throws ex
                val instance = possibleCompanion.objectInstance!!
                // If an instance of the companion object can be obtained, accessibility depends on the KFunction
                instance to callable.isAccessible
            } catch (ex: IllegalAccessException) {
                // fallback for when an odd access exception happens through Kotlin reflection
                possibleCompanion.java.enclosingClass.fields
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

internal class ConstructorValueCreator<T>(override val callable: KFunction<T>) : ValueCreator<T>() {
    override val accessible: Boolean = callable.isAccessible

    init {
        callable.isAccessible = true
    }
}
