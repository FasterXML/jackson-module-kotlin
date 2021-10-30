package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

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

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun of(
            _withArgsCreator: AnnotatedWithParams, cache: ReflectionCache
        ): ValueCreator<*>? = when (_withArgsCreator) {
            is AnnotatedConstructor -> cache.kotlinFromJava(_withArgsCreator.annotated as Constructor<Any>)
                ?.let { ConstructorValueCreator(it) }
            is AnnotatedMethod -> cache.kotlinFromJava(_withArgsCreator.annotated as Method)
                ?.let { MethodValueCreator.of(it) }
            else -> throw IllegalStateException("Expected a constructor or method to create a Kotlin object, instead found ${_withArgsCreator.annotated.javaClass.name}")
        } // we cannot reflect this method so do the default Java-ish behavior
    }
}
