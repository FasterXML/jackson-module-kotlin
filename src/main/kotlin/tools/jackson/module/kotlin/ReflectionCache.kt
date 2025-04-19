package tools.jackson.module.kotlin

import com.fasterxml.jackson.annotation.OptBoolean
import tools.jackson.databind.introspect.AnnotatedConstructor
import tools.jackson.databind.introspect.AnnotatedMember
import tools.jackson.databind.introspect.AnnotatedMethod
import tools.jackson.databind.introspect.AnnotatedParameter
import tools.jackson.databind.introspect.AnnotatedWithParams
import tools.jackson.databind.util.SimpleLookupCache
import java.io.Serializable
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.kotlinFunction

internal class ReflectionCache(reflectionCacheSize: Int) : Serializable {
    companion object {
        // Increment is required when properties that use LRUMap are changed.
        private const val serialVersionUID = 4L
    }

    private val javaExecutableToKotlin = SimpleLookupCache<Executable, KFunction<*>>(reflectionCacheSize, reflectionCacheSize)
    private val javaExecutableToValueCreator = SimpleLookupCache<Executable, ValueCreator<*>>(reflectionCacheSize, reflectionCacheSize)
    private val javaMemberIsRequired = SimpleLookupCache<AnnotatedMember, OptBoolean?>(reflectionCacheSize, reflectionCacheSize)

    // Initial size is 0 because the value class is not always used
    private val valueClassReturnTypeCache: SimpleLookupCache<AnnotatedMethod, Optional<KClass<*>>> =
        SimpleLookupCache(0, reflectionCacheSize)

    // TODO: Consider whether the cache size should be reduced more,
    //       since the cache is used only twice locally at initialization per property.
    private val valueClassBoxConverterCache: SimpleLookupCache<KClass<*>, ValueClassBoxConverter<*, *>> =
        SimpleLookupCache(0, reflectionCacheSize)

    // If the Record type defined in Java is processed,
    // an error will occur, so if it is not defined in Kotlin, skip the process.
    // see https://github.com/FasterXML/jackson-module-kotlin/issues/778
    fun kotlinFromJava(key: Constructor<*>): KFunction<*>? = if (key.declaringClass.isKotlinClass()) {
        javaExecutableToKotlin.get(key)
            ?: key.valueClassAwareKotlinFunction()?.let { javaExecutableToKotlin.putIfAbsent(key, it) ?: it }
    } else {
        null
    }

    fun kotlinFromJava(key: Method): KFunction<*>? = javaExecutableToKotlin.get(key)
        ?: key.kotlinFunction?.let { javaExecutableToKotlin.putIfAbsent(key, it) ?: it }

    /**
     * return null if...
     * - can't get kotlinFunction
     * - contains extensionReceiverParameter
     * - instance parameter is not companion object or can't get
     */
    fun valueCreatorFromJava(_withArgsCreator: AnnotatedWithParams): ValueCreator<*>? = when (_withArgsCreator) {
        is AnnotatedConstructor -> {
            val constructor = _withArgsCreator.annotated

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

    fun javaMemberIsRequired(key: AnnotatedMember, calc: (AnnotatedMember) -> Boolean?): Boolean? = javaMemberIsRequired.get(key)?.asBoolean()
            ?: calc(key).let { javaMemberIsRequired.putIfAbsent(key, OptBoolean.fromBoolean(it))?.asBoolean() ?: it }

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
                (prop?.returnType ?: runCatching { kotlinFromJava(getter) }.getOrNull()?.returnType)
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

    fun getValueClassBoxConverter(unboxedClass: Class<*>, boxedClass: KClass<*>): ValueClassBoxConverter<*, *> =
        valueClassBoxConverterCache.get(boxedClass) ?: run {
            val value = ValueClassBoxConverter(unboxedClass, boxedClass)
            (valueClassBoxConverterCache.putIfAbsent(boxedClass, value) ?: value)
        }

    fun findKotlinParameter(param: AnnotatedParameter): KParameter? = when (val owner = param.owner.member) {
        is Constructor<*> -> kotlinFromJava(owner)
        is Method -> kotlinFromJava(owner)
        else -> null
    }
        ?.valueParameters
        // Functions defined in value class may have a different index when retrieved as KFunction,
        // so use getOrNull to avoid errors.
        ?.getOrNull(param.index)
}

private fun Constructor<*>.valueClassAwareKotlinFunction(): KFunction<*>? {
    kotlinFunction?.apply { return this }

    // The javaConstructor that corresponds to the KFunction of the constructor that
    // takes value class as an argument is a synthetic constructor.
    // Therefore, in Kotlin 1.5.30, KFunction cannot be obtained from a constructor that is processed
    // by jackson-module-kotlin.
    // To deal with this situation, a synthetic constructor is obtained and a KFunction is obtained from it.
    return try {
        // The arguments of the synthetic constructor are the normal constructor arguments
        // with the DefaultConstructorMarker appended to the end.
        declaringClass
            .getDeclaredConstructor(*parameterTypes, defaultConstructorMarker)
            .kotlinFunction
    } catch (_: Throwable) {
        null
    }
}
