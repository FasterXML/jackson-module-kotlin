package tools.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.databind.JavaType
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.Annotated
import tools.jackson.databind.introspect.AnnotatedClass
import tools.jackson.databind.introspect.AnnotatedMember
import tools.jackson.databind.introspect.AnnotatedMethod
import tools.jackson.databind.introspect.AnnotatedParameter
import tools.jackson.databind.introspect.NopAnnotationIntrospector
import tools.jackson.databind.introspect.PotentialCreator
import java.lang.reflect.Constructor
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaType

internal class KotlinNamesAnnotationIntrospector(
    private val cache: ReflectionCache,
    private val kotlinPropertyNameAsImplicitName: Boolean
) : NopAnnotationIntrospector() {
    private fun getterNameFromJava(member: AnnotatedMethod): String? {
        val name = member.name

        // The reason for truncating after `-` is to truncate the random suffix
        // given after the value class accessor name.
        return when {
            name.startsWith("get") -> name.takeIf { it.contains("-") }?.let { _ ->
                name.substringAfter("get")
                    .replaceFirstChar { it.lowercase(Locale.getDefault()) }
                    .substringBefore('-')
            }
            // since 2.15: support Kotlin's way of handling "isXxx" backed properties where
            // logical property name needs to remain "isXxx" and not become "xxx" as with Java Beans
            // (see https://kotlinlang.org/docs/reference/java-to-kotlin-interop.html and
            //  https://github.com/FasterXML/jackson-databind/issues/2527 and
            //  https://github.com/FasterXML/jackson-module-kotlin/issues/340
            //  for details)
            name.startsWith("is") -> if (name.contains("-")) name.substringAfter("-") else name
            else -> null
        }
    }

    private fun getterNameFromKotlin(member: AnnotatedMethod): String? {
        val getterName = member.member.name

        return member.member.declaringClass.takeIf { it.isKotlinClass() }?.let { clazz ->
            // For edge case, methods must be compared by name, not directly.
            clazz.kotlin.memberProperties.find { it.javaGetter?.name == getterName }
                ?.let { it.name }
        }
    }

    // since 2.4
    override fun findImplicitPropertyName(config: MapperConfig<*>, member: AnnotatedMember): String? {
        if (!member.declaringClass.isKotlinClass()) return null

        return when (member) {
            is AnnotatedMethod -> if (member.parameterCount == 0) {
                if (kotlinPropertyNameAsImplicitName) {
                    // Fall back to default if it is a getter-like function
                    getterNameFromKotlin(member) ?: getterNameFromJava(member)
                } else getterNameFromJava(member)
            } else null
            is AnnotatedParameter -> findKotlinParameterName(member)
            else -> null
        }
    }

    override fun refineDeserializationType(config: MapperConfig<*>, a: Annotated, baseType: JavaType): JavaType =
        (a as? AnnotatedParameter)?.let { _ ->
            cache.findKotlinParameter(a)?.let { param ->
                val rawType = a.rawType
                (param.type.classifier as? KClass<*>)
                    ?.java
                    ?.takeIf { it.isUnboxableValueClass() && it != rawType }
                    ?.let { config.constructType(it) }
            }
        } ?: baseType

    override fun findDefaultCreator(
        config: MapperConfig<*>,
        valueClass: AnnotatedClass,
        declaredConstructors: List<PotentialCreator>,
        declaredFactories: List<PotentialCreator>
    ): PotentialCreator? {
        val kClass = valueClass.creatableKotlinClass() ?: return null

        val propertyNames = kClass.memberProperties.map { it.name }.toSet()

        val defaultCreator = kClass.let { _ ->
            // By default, the primary constructor or the only publicly available constructor may be used
            val ctor = kClass.primaryConstructor ?: kClass.constructors.takeIf { it.size == 1 }?.single()
            ctor?.takeIf { it.isPossibleCreator(propertyNames) }
        }
            ?: return null

        return declaredConstructors.find {
            // To avoid problems with constructors that include `value class` as an argument,
            // convert to `KFunction` and compare
            cache.kotlinFromJava(it.creator().annotated as Constructor<*>) == defaultCreator
        }
    }

    private fun findKotlinParameterName(param: AnnotatedParameter): String? = cache.findKotlinParameter(param)?.name
}

// If it is not a Kotlin class or an Enum, Creator is not used
private fun AnnotatedClass.creatableKotlinClass(): KClass<*>? = annotated
    .takeIf { it.isKotlinClass() && !it.isEnum }
    ?.kotlin

private fun KFunction<*>.isPossibleCreator(propertyNames: Set<String>): Boolean = 0 < parameters.size
    && !isPossibleSingleString(propertyNames)
    && parameters.none { it.name == null }

private fun KFunction<*>.isPossibleSingleString(propertyNames: Set<String>): Boolean = parameters.singleOrNull()?.let {
    it.name !in propertyNames
        && it.type.javaType == String::class.java
        && !it.hasAnnotation<JsonProperty>()
} == true
