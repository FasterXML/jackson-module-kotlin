package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.introspect.AnnotatedField
import com.fasterxml.jackson.databind.introspect.AnnotatedMember
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.*


internal class KotlinAnnotationIntrospector(private val context: Module.SetupContext, private val cache: ReflectionCache, private val nullToEmptyCollection: Boolean, private val nullToEmptyMap: Boolean) : NopAnnotationIntrospector() {

    override fun hasRequiredMarker(m: AnnotatedMember): Boolean? {
        return cache.javaMemberIsRequired(m) {
            try {
                when {
                    nullToEmptyCollection && m.type.isCollectionLikeType -> false
                    nullToEmptyMap && m.type.isMapLikeType -> false
                    m.member.declaringClass.isKotlinClass() -> when (m) {
                        is AnnotatedField -> m.hasRequiredMarker()
                        is AnnotatedMethod -> m.hasRequiredMarker()
                        is AnnotatedParameter -> m.hasRequiredMarker()
                        else -> null
                    }
                    else -> null
                }
            } catch (ex: UnsupportedOperationException) {
                null
            }
        }
    }

    private fun AnnotatedField.hasRequiredMarker(): Boolean? {
        val byAnnotation = (member as Field).getAnnotationsByType(JsonProperty::class.java).firstOrNull()?.required
        val byNullability =  (member as Field).kotlinProperty?.returnType?.isRequired()

        return requiredAnnotationOrNullability(byAnnotation, byNullability)
    }

    private fun requiredAnnotationOrNullability(byAnnotation: Boolean?, byNullability: Boolean?): Boolean? {
        if (byAnnotation != null && byNullability != null) {
            return byAnnotation || byNullability
        } else if (byNullability != null) {
            return byNullability
        }
        return byAnnotation
    }

    private fun Method.isRequiredByAnnotation(): Boolean? =
        getAnnotationsByType(JsonProperty::class.java)?.firstOrNull()?.required

    private fun AnnotatedMethod.hasRequiredMarker(): Boolean? {
        // This could be a setter or a getter of a class property or
        // a setter-like/getter-like method.
        val paramGetter = this.getCorrespondingGetter()
        if (paramGetter != null) {
            val byAnnotation = paramGetter.javaGetter?.isRequiredByAnnotation()
            return requiredAnnotationOrNullability(byAnnotation, paramGetter.returnType.isRequired())
        }

        val paramSetter = this.getCorrespondingSetter()
        if (paramSetter != null) {
            val byAnnotation = paramSetter.javaMethod?.isRequiredByAnnotation()
            return requiredAnnotationOrNullability(byAnnotation, paramSetter.isConstructorParameterRequired(1)) // 0 is the target object
        }

        // Is the member method a regular method of the data class or
        val method = this.member.kotlinFunction
        if (method != null) {
            val byAnnotation = method.javaMethod?.isRequiredByAnnotation()
            if (method.isGetterLike()) {
                return requiredAnnotationOrNullability(byAnnotation, method.returnType.isRequired())
            }

            if (method.isSetterLike()) {
                return requiredAnnotationOrNullability(byAnnotation, method.isConstructorParameterRequired(1))
            }
        }

        return null
    }

    private fun KFunction<*>.isGetterLike(): Boolean = parameters.size == 1
    private fun KFunction<*>.isSetterLike(): Boolean =
            parameters.size == 2 && returnType == Unit::class.createType()


    private fun AnnotatedMethod.getCorrespondingGetter(): KProperty1<out Any, Any?>? =
            member.declaringClass.kotlin.declaredMemberProperties.find {
                it.getter.javaMethod == this.member
            }

    private fun AnnotatedMethod.getCorrespondingSetter(): KMutableProperty1.Setter<out Any, Any?>? {
        val mutableProperty = member.declaringClass.kotlin.declaredMemberProperties.find {
            when (it) {
                is KMutableProperty1 -> it.javaSetter == this.member
                else                 -> false
            }
        }
        return (mutableProperty as? KMutableProperty1<out Any, Any?>)?.setter
    }

    private fun AnnotatedParameter.hasRequiredMarker(): Boolean? {
        val member = this.member
        val byAnnotation = this.getAnnotation(JsonProperty::class.java)?.required
        val byNullability = when (member) {
            is Constructor<*> -> member.kotlinFunction?.isConstructorParameterRequired(index)
            is Method         -> member.kotlinFunction?.isMethodParameterRequired(index)
            else              -> null
        }

        return requiredAnnotationOrNullability(byAnnotation, byNullability)
    }

    private fun KFunction<*>.isMethodParameterRequired(index: Int): Boolean {
        // First parameter in this case is the instance of the function.
        // We want to adjust our index to handle this case.
        val adjustedIndex = if (parameters[0] == instanceParameter) {
            index+1
        } else {
            index
        }
        val param = parameters[adjustedIndex]
        val paramType = param.type
        val javaType = paramType.javaType
        val isPrimitive = when (javaType) {
            is Class<*> -> javaType.isPrimitive
            else -> false
        }

        return !paramType.isMarkedNullable && !param.isOptional &&
                !(isPrimitive && !context.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES))
    }

    private fun KFunction<*>.isConstructorParameterRequired(index: Int): Boolean {
        val param = parameters[index]
        val paramType = param.type
        val javaType = paramType.javaType
        val isPrimitive = when (javaType) {
            is Class<*> -> javaType.isPrimitive
            else -> false
        }

        return !paramType.isMarkedNullable && !param.isOptional &&
                !(isPrimitive && !context.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES))
    }

    private fun KType.isRequired(): Boolean = !isMarkedNullable

}
