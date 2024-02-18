package tools.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.databind.JavaType
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.Annotated
import tools.jackson.databind.introspect.AnnotatedConstructor
import tools.jackson.databind.introspect.AnnotatedMember
import tools.jackson.databind.introspect.AnnotatedMethod
import tools.jackson.databind.introspect.AnnotatedParameter
import tools.jackson.databind.introspect.NopAnnotationIntrospector
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaType

internal class KotlinNamesAnnotationIntrospector(
    private val cache: ReflectionCache,
    private val ignoredClassesForImplyingJsonCreator: Set<KClass<*>>,
    private val useKotlinPropertyNameForGetter: Boolean
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
                if (useKotlinPropertyNameForGetter) {
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

    private fun hasCreatorAnnotation(member: AnnotatedConstructor): Boolean {
        // don't add a JsonCreator to any constructor if one is declared already

        val kClass = member.declaringClass.kotlin
            .apply { if (this in ignoredClassesForImplyingJsonCreator) return false }
        val kConstructor = cache.kotlinFromJava(member.annotated) ?: return false

        // TODO:  should we do this check or not?  It could cause failures if we miss another way a property could be set
        // val requiredProperties = kClass.declaredMemberProperties.filter {!it.returnType.isMarkedNullable }.map { it.name }.toSet()
        // val areAllRequiredParametersInConstructor = kConstructor.parameters.all { requiredProperties.contains(it.name) }

        val propertyNames = kClass.memberProperties.map { it.name }.toSet()

        return when {
            kConstructor.isPossibleSingleString(propertyNames) -> false
            kConstructor.parameters.any { it.name == null } -> false
            !kClass.isPrimaryConstructor(kConstructor) -> false
            else -> {
                val anyConstructorHasJsonCreator = kClass.constructors
                    .filterOutSingleStringCallables(propertyNames)
                    .any { it.hasAnnotation<JsonCreator>() }

                val anyCompanionMethodIsJsonCreator = member.type.rawClass.kotlin.companionObject?.declaredFunctions
                    ?.filterOutSingleStringCallables(propertyNames)
                    ?.any { it.hasAnnotation<JsonCreator>() && it.hasAnnotation<JvmStatic>() }
                    ?: false

                !(anyConstructorHasJsonCreator || anyCompanionMethodIsJsonCreator)
            }
        }
    }

    // TODO: possible work around for JsonValue class that requires the class constructor to have the JsonCreator(DELEGATED) set?
    //   since we infer the creator at times for these methods, the wrong mode could be implied.
    override fun findCreatorAnnotation(config: MapperConfig<*>, ann: Annotated): JsonCreator.Mode? {
        if (ann !is AnnotatedConstructor || !ann.isKotlinConstructorWithParameters()) return null

        return JsonCreator.Mode.DEFAULT.takeIf {
            cache.checkConstructorIsCreatorAnnotated(ann) { hasCreatorAnnotation(it) }
        }
    }

    private fun findKotlinParameterName(param: AnnotatedParameter): String? = cache.findKotlinParameter(param)?.name
}

// if has parameters, is a Kotlin class, and the parameters all have parameter annotations, then pretend we have a JsonCreator
private fun AnnotatedConstructor.isKotlinConstructorWithParameters(): Boolean =
    parameterCount > 0 && declaringClass.isKotlinClass() && !declaringClass.isEnum

private fun KFunction<*>.isPossibleSingleString(propertyNames: Set<String>): Boolean = parameters.size == 1 &&
        parameters[0].name !in propertyNames &&
        parameters[0].type.javaType == String::class.java &&
        !parameters[0].hasAnnotation<JsonProperty>()

private fun Collection<KFunction<*>>.filterOutSingleStringCallables(propertyNames: Set<String>): Collection<KFunction<*>> =
    this.filter { !it.isPossibleSingleString(propertyNames) }

private fun KClass<*>.isPrimaryConstructor(kConstructor: KFunction<*>) = this.primaryConstructor.let {
    it == kConstructor || (it == null && this.constructors.size == 1)
}
