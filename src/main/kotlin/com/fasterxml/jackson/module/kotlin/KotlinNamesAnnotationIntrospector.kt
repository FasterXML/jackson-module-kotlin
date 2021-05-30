package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyName
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor
import com.fasterxml.jackson.databind.introspect.AnnotatedField
import com.fasterxml.jackson.databind.introspect.AnnotatedMember
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector
import com.fasterxml.jackson.databind.util.BeanUtil
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

internal class KotlinNamesAnnotationIntrospector(val module: KotlinModule, val cache: ReflectionCache, val ignoredClassesForImplyingJsonCreator: Set<KClass<*>>) : NopAnnotationIntrospector() {
    // since 2.4
    override fun findImplicitPropertyName(member: AnnotatedMember): String? = when (member) {
        is AnnotatedMethod -> if (member.name.contains('-') && member.parameterCount == 0) {
            when {
                member.name.startsWith("get") -> member.name.substringAfter("get")
                member.name.startsWith("is") -> member.name.substringAfter("is")
                else -> null
            }?.replaceFirstChar { it.lowercase(Locale.getDefault()) }?.substringBefore('-')
        } else null
        is AnnotatedParameter -> findKotlinParameterName(member)
        else -> null
    }

    // since 2.11: support Kotlin's way of handling "isXxx" backed properties where
    // logical property name needs to remain "isXxx" and not become "xxx" as with Java Beans
    // (see https://kotlinlang.org/docs/reference/java-to-kotlin-interop.html and
    //  https://github.com/FasterXML/jackson-databind/issues/2527
    //  for details)
    override fun findRenameByField(config: MapperConfig<*>,
                                   field: AnnotatedField,
                                   implName: PropertyName): PropertyName? {
        val origSimple = implName.simpleName
        if (field.declaringClass.isKotlinClass() && origSimple.startsWith("is")) {
            val mangledName: String? = BeanUtil.stdManglePropertyName(origSimple, 2)
            if ((mangledName != null) && !mangledName.equals(origSimple)) {
                return PropertyName.construct(mangledName)
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasCreatorAnnotation(member: Annotated): Boolean {
        // don't add a JsonCreator to any constructor if one is declared already

        if (member is AnnotatedConstructor && !member.declaringClass.isEnum) {
            // if has parameters, is a Kotlin class, and the parameters all have parameter annotations, then pretend we have a JsonCreator
            if (member.parameterCount > 0 && member.declaringClass.isKotlinClass()) {
                return cache.checkConstructorIsCreatorAnnotated(member) {
                    val kClass = cache.kotlinFromJava(member.declaringClass as Class<Any>)
                    val kConstructor = cache.kotlinFromJava(member.annotated as Constructor<Any>)

                    if (kConstructor != null) {
                        val isPrimaryConstructor = kClass.primaryConstructor == kConstructor ||
                                (kClass.primaryConstructor == null && kClass.constructors.size == 1)

                        val propertyNames = kClass.memberProperties.map { it.name }.toSet()

                        fun KFunction<*>.isPossibleSingleString(): Boolean {
                            val result = parameters.size == 1 &&
                                    parameters[0].name !in propertyNames &&
                                    parameters[0].type.javaType == String::class.java &&
                                    parameters[0].annotations.none { it.annotationClass.java == JsonProperty::class.java }
                            return result
                        }

                        fun Collection<KFunction<*>>.filterOutSingleStringCallables(): Collection<KFunction<*>> {
                            return this.filter { !it.isPossibleSingleString() }
                        }

                        val anyConstructorHasJsonCreator = kClass.constructors.filterOutSingleStringCallables()
                                .any { it.annotations.any { it.annotationClass.java == JsonCreator::class.java }
                                }

                        val anyCompanionMethodIsJsonCreator = member.type.rawClass.kotlin.companionObject?.declaredFunctions
                                ?.filterOutSingleStringCallables()?.any {
                                    it.annotations.any { it.annotationClass.java == JvmStatic::class.java } &&
                                            it.annotations.any { it.annotationClass.java == JsonCreator::class.java }
                                } ?: false

                        // TODO:  should we do this check or not?  It could cause failures if we miss another way a property could be set
                        // val requiredProperties = kClass.declaredMemberProperties.filter {!it.returnType.isMarkedNullable }.map { it.name }.toSet()
                        // val areAllRequiredParametersInConstructor = kConstructor.parameters.all { requiredProperties.contains(it.name) }

                        val areAllParametersValid = kConstructor.parameters.size == kConstructor.parameters.count { it.name != null }

                        val isSingleStringConstructor = kConstructor.isPossibleSingleString()

                        val implyCreatorAnnotation = isPrimaryConstructor
                                && !(anyConstructorHasJsonCreator || anyCompanionMethodIsJsonCreator)
                                && areAllParametersValid
                                && !isSingleStringConstructor
                                && kClass !in ignoredClassesForImplyingJsonCreator

                        implyCreatorAnnotation
                    } else {
                        false
                    }
                }
            }
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun findKotlinParameterName(param: AnnotatedParameter): String? {
        return if (param.declaringClass.isKotlinClass()) {
            val member = param.owner.member
            if (member is Constructor<*>) {
                val ctor = (member as Constructor<Any>)
                val ctorParmCount = ctor.parameterTypes.size
                val ktorParmCount = try { ctor.kotlinFunction?.parameters?.size ?: 0 }
                catch (ex: KotlinReflectionInternalError) { 0 }
                catch (ex: UnsupportedOperationException) { 0 }
                if (ktorParmCount > 0 && ktorParmCount == ctorParmCount) {
                    ctor.kotlinFunction?.parameters?.get(param.index)?.name
                } else {
                    null
                }
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
