package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor
import com.fasterxml.jackson.databind.introspect.AnnotatedMember
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams
import com.fasterxml.jackson.databind.util.LRUMap
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.kotlinFunction

internal class ReflectionCache(reflectionCacheSize: Int) {
    sealed class BooleanTriState(val value: Boolean?) {
        class True : BooleanTriState(true)
        class False : BooleanTriState(false)
        class Empty : BooleanTriState(null)

        companion object {
            private val TRUE = True()
            private val FALSE = False()
            private val EMPTY = Empty()

            fun fromBoolean(value: Boolean?): BooleanTriState {
                return when (value) {
                    null -> EMPTY
                    true -> TRUE
                    false -> FALSE
                }
            }
        }
    }

    private val javaClassToKotlin = LRUMap<Class<Any>, KClass<Any>>(reflectionCacheSize, reflectionCacheSize)
    private val javaConstructorToKotlin = LRUMap<Constructor<Any>, KFunction<Any>>(reflectionCacheSize, reflectionCacheSize)
    private val javaMethodToKotlin = LRUMap<Method, KFunction<*>>(reflectionCacheSize, reflectionCacheSize)
    private val javaConstructorIsCreatorAnnotated = LRUMap<AnnotatedConstructor, Boolean>(reflectionCacheSize, reflectionCacheSize)
    private val javaMemberIsRequired = LRUMap<AnnotatedMember, BooleanTriState?>(reflectionCacheSize, reflectionCacheSize)
    private val kotlinGeneratedMethod = LRUMap<AnnotatedMethod, Boolean>(reflectionCacheSize, reflectionCacheSize)
    private val javaConstructorToInstantiator = LRUMap<Constructor<Any>, ConstructorInstantiator<Any>>(reflectionCacheSize, reflectionCacheSize)
    private val javaMethodToInstantiator = LRUMap<Method, MethodInstantiator<*>>(reflectionCacheSize, reflectionCacheSize)

    fun kotlinFromJava(key: Class<Any>): KClass<Any> = javaClassToKotlin.get(key)
            ?: key.kotlin.let { javaClassToKotlin.putIfAbsent(key, it) ?: it }

    fun kotlinFromJava(key: Constructor<Any>): KFunction<Any>? = javaConstructorToKotlin.get(key)
            ?: key.kotlinFunction?.let { javaConstructorToKotlin.putIfAbsent(key, it) ?: it }

    fun kotlinFromJava(key: Method): KFunction<*>? = javaMethodToKotlin.get(key)
            ?: key.kotlinFunction?.let { javaMethodToKotlin.putIfAbsent(key, it) ?: it }

    fun checkConstructorIsCreatorAnnotated(key: AnnotatedConstructor, calc: (AnnotatedConstructor) -> Boolean): Boolean = javaConstructorIsCreatorAnnotated.get(key)
            ?: calc(key).let { javaConstructorIsCreatorAnnotated.putIfAbsent(key, it) ?: it }

    fun javaMemberIsRequired(key: AnnotatedMember, calc: (AnnotatedMember) -> Boolean?): Boolean? = javaMemberIsRequired.get(key)?.value
            ?: calc(key).let { javaMemberIsRequired.putIfAbsent(key, BooleanTriState.fromBoolean(it))?.value ?: it }

    fun isKotlinGeneratedMethod(key: AnnotatedMethod, calc: (AnnotatedMethod) -> Boolean): Boolean = kotlinGeneratedMethod.get(key)
            ?: calc(key).let { kotlinGeneratedMethod.putIfAbsent(key, it) ?: it }

    private fun instantiatorFromJavaConstructor(key: Constructor<Any>): ConstructorInstantiator<*>? = javaConstructorToInstantiator.get(key)
        ?: kotlinFromJava(key)?.let {
            val instantiator = ConstructorInstantiator(it, key)
            javaConstructorToInstantiator.putIfAbsent(key, instantiator) ?: instantiator
        }

    private fun instantiatorFromJavaMethod(key: Method): MethodInstantiator<*>? = javaMethodToInstantiator.get(key)
        ?: kotlinFromJava(key)?.takeIf {
            // we shouldn't have an instance or receiver parameter and if we do, just go with default Java-ish behavior
            it.extensionReceiverParameter == null
        }?.let { callable ->
            var companionInstance: Any? = null
            var companionAccessible: Boolean? = null

            callable.instanceParameter!!.type.erasedType().kotlin
                .takeIf { it.isCompanion } // abort, we have some unknown case here
                ?.let { possibleCompanion ->
                    try {
                        companionInstance = possibleCompanion.objectInstance
                        companionAccessible = true
                    } catch (ex: IllegalAccessException) {
                        // fallback for when an odd access exception happens through Kotlin reflection
                        possibleCompanion.java.enclosingClass.fields
                            .firstOrNull { it.type.kotlin.isCompanion }
                            ?.let {
                                companionAccessible = it.isAccessible
                                it.isAccessible = true

                                companionInstance = it.get(null)
                            } ?: throw ex
                    }
                }

            companionInstance?.let {
                MethodInstantiator(callable, key, it, companionAccessible!!).run {
                    javaMethodToInstantiator.putIfAbsent(key, this) ?: this
                }
            }
        }

    /*
     * return null if...
     * - can't get kotlinFunction
     * - contains extensionReceiverParameter
     * - instance parameter is not companion object or can't get
     */
    @Suppress("UNCHECKED_CAST")
    fun instantiatorFromJava(_withArgsCreator: AnnotatedWithParams): Instantiator<*>? = when (_withArgsCreator) {
        is AnnotatedConstructor -> instantiatorFromJavaConstructor(_withArgsCreator.annotated as Constructor<Any>)
        is AnnotatedMethod -> instantiatorFromJavaMethod(_withArgsCreator.annotated as Method)
        else ->
            throw IllegalStateException("Expected a constructor or method to create a Kotlin object, instead found ${_withArgsCreator.annotated.javaClass.name}")
    }
}
