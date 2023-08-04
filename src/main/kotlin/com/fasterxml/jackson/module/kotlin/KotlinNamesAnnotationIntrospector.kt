package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor
import com.fasterxml.jackson.databind.introspect.AnnotatedMember
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

internal class KotlinNamesAnnotationIntrospector(
    val module: KotlinModule,
    val cache: ReflectionCache,
    val ignoredClassesForImplyingJsonCreator: Set<KClass<*>>,
    val useKotlinPropertyNameForGetter: Boolean
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
        val getter = member.member

        return member.member.declaringClass.takeIf { it.isKotlinClass() }?.let { clazz ->
            clazz.kotlin.memberProperties.find { it.javaGetter == getter }
                ?.let { it.name }
        }
    }

    // since 2.4
    override fun findImplicitPropertyName(member: AnnotatedMember): String? {
        if (member.rawType.isKotlinDefaultConstructorMarker) {
            return "DefaultConstructorMarker"
        }

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

    @Suppress("UNCHECKED_CAST")
    private fun hasCreatorAnnotation(member: AnnotatedConstructor): Boolean {
        // don't add a JsonCreator to any constructor if one is declared already

        val kClass = member.declaringClass.kotlin
            .apply { if (this in ignoredClassesForImplyingJsonCreator) return false }
        val kConstructor = cache.kotlinFromJava(member.annotated as Constructor<Any>) ?: return false

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

    override fun hasCreatorAnnotation(member: Annotated): Boolean =
        if (member is AnnotatedConstructor && member.isKotlinConstructorWithParameters())
            cache.checkConstructorIsCreatorAnnotated(member) { hasCreatorAnnotation(it) }
        else
            member.isJvmInlineClassSyntheticBoxingFunction

    private val Annotated.isJvmInlineClassSyntheticBoxingFunction: Boolean
        get() = name == "box-impl"

    @Suppress("UNCHECKED_CAST")
    private fun findKotlinParameterName(param: AnnotatedParameter): String? {
        return if (param.declaringClass.isKotlinClass()) {
            val member = param.owner.member
            if (member is Constructor<*>) {
                member.kotlinFunction?.parameters?.elementAtOrNull(param.index)?.name
            } else if (member is Method) {
                try {
                    val temp = member.kotlinFunction

                    val firstParamKind = temp?.parameters?.firstOrNull()?.kind
                    val idx = if (firstParamKind != KParameter.Kind.VALUE) param.index + 1 else param.index
                    val parmCount = temp?.parameters?.size ?: 0
                    if (parmCount > idx) {
                        temp?.parameters?.get(idx)?.name
                    } else {
                        null
                    }
                } catch (ex: KotlinReflectionInternalError) {
                    null
                }
            } else {
                null
            }
        } else {
            null
        }
    }
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
