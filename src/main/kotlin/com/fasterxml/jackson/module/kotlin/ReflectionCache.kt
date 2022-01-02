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
    private val javaConstructorToValueCreator = LRUMap<Constructor<Any>, ConstructorValueCreator<*>>(reflectionCacheSize, reflectionCacheSize)
    private val javaMethodToValueCreator = LRUMap<Method, MethodValueCreator<*>>(reflectionCacheSize, reflectionCacheSize)
    private val javaConstructorIsCreatorAnnotated = LRUMap<AnnotatedConstructor, Boolean>(reflectionCacheSize, reflectionCacheSize)
    private val javaMemberIsRequired = LRUMap<AnnotatedMember, BooleanTriState?>(reflectionCacheSize, reflectionCacheSize)
    private val kotlinGeneratedMethod = LRUMap<AnnotatedMethod, Boolean>(reflectionCacheSize, reflectionCacheSize)


    fun kotlinFromJava(key: Class<Any>): KClass<Any> = javaClassToKotlin.get(key)
            ?: key.kotlin.let { javaClassToKotlin.putIfAbsent(key, it) ?: it }

    fun kotlinFromJava(key: Constructor<Any>): KFunction<Any>? = javaConstructorToKotlin.get(key)
            ?: key.kotlinFunction?.let { javaConstructorToKotlin.putIfAbsent(key, it) ?: it }

    fun kotlinFromJava(key: Method): KFunction<*>? = javaMethodToKotlin.get(key)
            ?: key.kotlinFunction?.let { javaMethodToKotlin.putIfAbsent(key, it) ?: it }

    /**
     * return null if...
     * - can't get kotlinFunction
     * - contains extensionReceiverParameter
     * - instance parameter is not companion object or can't get
     */
    @Suppress("UNCHECKED_CAST")
    fun valueCreatorFromJava(_withArgsCreator: AnnotatedWithParams): ValueCreator<*>? = when (_withArgsCreator) {
        is AnnotatedConstructor -> {
            val constructor = _withArgsCreator.annotated as Constructor<Any>

            javaConstructorToValueCreator.get(constructor)
                ?: kotlinFromJava(constructor)?.let {
                    val value = ConstructorValueCreator(it)

                    javaConstructorToValueCreator.putIfAbsent(constructor, value) ?: value
                }
        }
        is AnnotatedMethod -> {
            val method = _withArgsCreator.annotated as Method

            javaMethodToValueCreator.get(method)
                ?: kotlinFromJava(method)?.let {
                    val value = MethodValueCreator.of(it)
                    javaMethodToValueCreator.putIfAbsent(method, value) ?: value
                }
        }
        else -> throw IllegalStateException("Expected a constructor or method to create a Kotlin object, instead found ${_withArgsCreator.annotated.javaClass.name}")
    } // we cannot reflect this method so do the default Java-ish behavior

    fun checkConstructorIsCreatorAnnotated(key: AnnotatedConstructor, calc: (AnnotatedConstructor) -> Boolean): Boolean = javaConstructorIsCreatorAnnotated.get(key)
            ?: calc(key).let { javaConstructorIsCreatorAnnotated.putIfAbsent(key, it) ?: it }

    fun javaMemberIsRequired(key: AnnotatedMember, calc: (AnnotatedMember) -> Boolean?): Boolean? = javaMemberIsRequired.get(key)?.value
            ?: calc(key).let { javaMemberIsRequired.putIfAbsent(key, BooleanTriState.fromBoolean(it))?.value ?: it }

    fun isKotlinGeneratedMethod(key: AnnotatedMethod, calc: (AnnotatedMethod) -> Boolean): Boolean = kotlinGeneratedMethod.get(key)
            ?: calc(key).let { kotlinGeneratedMethod.putIfAbsent(key, it) ?: it }
}
