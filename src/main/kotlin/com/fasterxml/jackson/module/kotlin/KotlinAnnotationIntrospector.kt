package com.fasterxml.jackson.module.kotlin

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
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.jvm.kotlinProperty


internal class KotlinAnnotationIntrospector(private val context: Module.SetupContext) : NopAnnotationIntrospector() {


    override fun hasRequiredMarker(m: AnnotatedMember): Boolean? =
            if (m.member.declaringClass.isKotlinClass()) {
                when (m) {
                    is AnnotatedField     -> m.hasRequiredMarker()
                    is AnnotatedMethod    -> m.hasRequiredMarker()
                    is AnnotatedParameter -> m.hasRequiredMarker()
                    else                  -> null
                }
            } else {
                null
            }

    private fun AnnotatedField.hasRequiredMarker(): Boolean? =
            (member as Field).kotlinProperty?.returnType?.isRequired()

    private fun AnnotatedMethod.hasRequiredMarker(): Boolean? {
        // This could be a setter or a getter of a class property or
        // a setter-like/getter-like method.
        val paramGetter = this.getCorrespondingGetter()
        if (paramGetter != null) {
            return paramGetter.returnType.isRequired()
        }

        val paramSetter = this.getCorrespondingSetter()
        if (paramSetter != null) {
            return paramSetter.isParameterRequired(1) // 0 is the target object
        }

        // Is the member method a regular method of the data class or
        val method = this.member.kotlinFunction
        if (method != null) {
            if (method.isGetterLike()) {
                return method.returnType.isRequired()
            }

            if (method.isSetterLike()) {
                return method.isParameterRequired(1)
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
        return when (member) {
            is Constructor<*> -> member.kotlinFunction?.isParameterRequired(index)
            is Method         -> member.kotlinFunction?.isParameterRequired(index)
            else              -> null
        }
    }

    private fun KFunction<*>.isParameterRequired(index: Int): Boolean {
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
