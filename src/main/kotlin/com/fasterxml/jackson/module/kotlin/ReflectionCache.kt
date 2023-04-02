package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor
import com.fasterxml.jackson.databind.introspect.AnnotatedMember
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams
import com.fasterxml.jackson.databind.util.LRUMap
import java.io.Serializable
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.kotlinFunction

internal class ReflectionCache(reflectionCacheSize: Int) : Serializable {
    companion object {
        // Increment is required when properties that use LRUMap are changed.
        private const val serialVersionUID = 1L
    }

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

    private val javaConstructorToKotlin = LRUMap<Constructor<Any>, KFunction<Any>>(reflectionCacheSize, reflectionCacheSize)
    private val javaMethodToKotlin = LRUMap<Method, KFunction<*>>(reflectionCacheSize, reflectionCacheSize)
    private val javaExecutableToValueCreator = LRUMap<Executable, ValueCreator<*>>(reflectionCacheSize, reflectionCacheSize)
    private val javaConstructorIsCreatorAnnotated = LRUMap<AnnotatedConstructor, Boolean>(reflectionCacheSize, reflectionCacheSize)
    private val javaMemberIsRequired = LRUMap<AnnotatedMember, BooleanTriState?>(reflectionCacheSize, reflectionCacheSize)

    // Initial size is 0 because the value class is not always used
    private val valueClassReturnTypeCache: LRUMap<AnnotatedMethod, Optional<KClass<*>>> =
        LRUMap(0, reflectionCacheSize)

    // TODO: Consider whether the cache size should be reduced more,
    //       since the cache is used only twice locally at initialization per property.
    private val valueClassBoxConverterCache: LRUMap<KClass<*>, ValueClassBoxConverter<*, *>> =
        LRUMap(0, reflectionCacheSize)

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

            javaExecutableToValueCreator.get(constructor)
                ?: kotlinFromJava(constructor)?.let {
                    val value = ConstructorValueCreator(it)
                    javaExecutableToValueCreator.putIfAbsent(constructor, value) ?: value
                }
        }
        is AnnotatedMethod -> {
            val method = _withArgsCreator.annotated

            javaExecutableToValueCreator.get(method)
                ?: kotlinFromJava(method)?.let {
                    val value = MethodValueCreator.of(it)
                    javaExecutableToValueCreator.putIfAbsent(method, value) ?: value
                }
        }
        else -> throw IllegalStateException(
            "Expected a constructor or method to create a Kotlin object," +
                    " instead found ${_withArgsCreator.annotated.javaClass.name}"
        )
    } // we cannot reflect this method so do the default Java-ish behavior

    fun checkConstructorIsCreatorAnnotated(key: AnnotatedConstructor, calc: (AnnotatedConstructor) -> Boolean): Boolean = javaConstructorIsCreatorAnnotated.get(key)
            ?: calc(key).let { javaConstructorIsCreatorAnnotated.putIfAbsent(key, it) ?: it }

    fun javaMemberIsRequired(key: AnnotatedMember, calc: (AnnotatedMember) -> Boolean?): Boolean? = javaMemberIsRequired.get(key)?.value
            ?: calc(key).let { javaMemberIsRequired.putIfAbsent(key, BooleanTriState.fromBoolean(it))?.value ?: it }

    private fun AnnotatedMethod.getValueClassReturnType(): KClass<*>? {
        val getter = this.member.apply {
            // If the return value of the getter is a value class,
            // it will be serialized properly without doing anything.
            // TODO: Verify the case where a value class encompasses another value class.
            if (this.returnType.isUnboxableValueClass()) return null
        }

        // Extract the return type from the property or getter-like.
        val kotlinReturnType = getter.declaringClass.kotlin
            .let { kClass ->
                // KotlinReflectionInternalError is raised in GitHub167 test,
                // but it looks like an edge case, so it is ignored.
                val prop = runCatching { kClass.memberProperties }.getOrNull()?.find { it.javaGetter == getter }
                (prop?.returnType ?: runCatching { getter.kotlinFunction }.getOrNull()?.returnType)
                    ?.classifier as? KClass<*>
            } ?: return null

        // Since there was no way to directly determine whether returnType is a value class or not,
        // Class is restored and processed.
        return kotlinReturnType.takeIf { it.isValue }
    }

    fun findValueClassReturnType(getter: AnnotatedMethod): KClass<*>? {
        val optional = valueClassReturnTypeCache.get(getter)

        return if (optional != null) {
            optional
        } else {
            val value = Optional.ofNullable(getter.getValueClassReturnType())
            (valueClassReturnTypeCache.putIfAbsent(getter, value) ?: value)
        }.orElse(null)
    }

    fun getValueClassBoxConverter(unboxedClass: Class<*>, valueClass: KClass<*>): ValueClassBoxConverter<*, *> =
        valueClassBoxConverterCache.get(valueClass) ?: run {
            val value = ValueClassBoxConverter(unboxedClass, valueClass)
            (valueClassBoxConverterCache.putIfAbsent(valueClass, value) ?: value)
        }
}
