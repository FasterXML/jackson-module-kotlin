package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JacksonModule
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.AnnotatedField
import com.fasterxml.jackson.databind.introspect.AnnotatedMember
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.jvm.kotlinProperty

internal class KotlinAnnotationIntrospector(private val context: JacksonModule.SetupContext,
                                            private val cache: ReflectionCache,
                                            private val nullToEmptyCollection: Boolean,
                                            private val nullToEmptyMap: Boolean,
                                            private val nullIsSameAsDefault: Boolean) : NopAnnotationIntrospector() {

    // TODO: implement nullIsSameAsDefault flag, which represents when TRUE that if something has a default value, it can be passed a null to default it
    //       this likely impacts this class to be accurate about what COULD be considered required

    override fun hasRequiredMarker(cfg : MapperConfig<*>, m: AnnotatedMember): Boolean? =
        cache.javaMemberIsRequired(m) {
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

    override fun findCreatorAnnotation(config: MapperConfig<*>, a: Annotated): JsonCreator.Mode? {

        // TODO: possible work around for JsonValue class that requires the class constructor to have the JsonCreator(Mode.DELEGATED) set?
        // since we infer the creator at times for these methods, the wrong mode could be implied.

        // findCreatorBinding used to be a clearer way to set this, but we need to set the mode here to disambugiate the intent of the constructor
        return super.findCreatorAnnotation(config, a)
    }

    // Find a serializer to handle the case where the getter returns an unboxed value from the value class.
    override fun findSerializer(config: MapperConfig<*>?, am: Annotated): StdSerializer<*>? = when (am) {
        is AnnotatedMethod -> {
            val getter = am.member.apply {
                // If the return value of the getter is a value class,
                // it will be serialized properly without doing anything.
                if (this.returnType.isUnboxableValueClass()) return null
            }

            val kotlinProperty = getter
                .declaringClass
                .kotlin
                .let {
                    // KotlinReflectionInternalError is raised in GitHub167 test,
                    // but it looks like an edge case, so it is ignored.
                    try {
                        it.memberProperties
                    } catch (e: Error) {
                        null
                    }
                }?.find { it.javaGetter == getter }

            (kotlinProperty?.returnType?.classifier as? KClass<*>)
                ?.takeIf { it.isValue }
                ?.java
                ?.let { outerClazz ->
                    val innerClazz = getter.returnType

                    ValueClassStaticJsonValueSerializer.createdOrNull(outerClazz, innerClazz)
                        ?: @Suppress("UNCHECKED_CAST") ValueClassBoxSerializer(outerClazz, innerClazz)
                }
        }
        // Ignore the case of AnnotatedField, because JvmField cannot be set in the field of value class.
        else -> null
    }

    // Perform proper serialization even if the value wrapped by the value class is null.
    override fun findNullSerializer(config: MapperConfig<*>?, am: Annotated) = findSerializer(config, am)

    /**
     * Subclasses can be detected automatically for sealed classes, since all possible subclasses are known
     * at compile-time to Kotlin. This makes [com.fasterxml.jackson.annotation.JsonSubTypes] redundant.
     */
    override fun findSubtypes(cfg : MapperConfig<*>, a: Annotated): MutableList<NamedType>? {
        return a.rawType
            .takeIf { it.isKotlinClass() }
            ?.let { rawType ->
                rawType.kotlin.sealedSubclasses
                    .map { NamedType(it.java) }
                    .toMutableList()
                    .ifEmpty { null }
            }
    }

    private fun AnnotatedField.hasRequiredMarker(): Boolean? {
        val byAnnotation = (member as Field).isRequiredByAnnotation()
        val byNullability =  (member as Field).kotlinProperty?.returnType?.isRequired()

        return requiredAnnotationOrNullability(byAnnotation, byNullability)
    }

    private fun AccessibleObject.isRequiredByAnnotation(): Boolean? = annotations
        ?.firstOrNull { it.annotationClass == JsonProperty::class }
        ?.let { it as JsonProperty }
        ?.required

    private fun requiredAnnotationOrNullability(byAnnotation: Boolean?, byNullability: Boolean?): Boolean? {
        if (byAnnotation != null && byNullability != null) {
            return byAnnotation || byNullability
        } else if (byNullability != null) {
            return byNullability
        }
        return byAnnotation
    }

    private fun Method.isRequiredByAnnotation(): Boolean? {
       return (this.annotations.firstOrNull { it.annotationClass.java == JsonProperty::class.java } as? JsonProperty)?.required
    }

    // Since Kotlin's property has the same Type for each field, getter, and setter,
    // nullability can be determined from the returnType of KProperty.
    private fun KProperty1<*, *>.isRequiredByNullability() = returnType.isRequired()

    // This could be a setter or a getter of a class property or
    // a setter-like/getter-like method.
    private fun AnnotatedMethod.hasRequiredMarker(): Boolean? = this.getRequiredMarkerFromCorrespondingAccessor()
        ?: this.member.getRequiredMarkerFromAccessorLikeMethod()

    private fun AnnotatedMethod.getRequiredMarkerFromCorrespondingAccessor(): Boolean? {
        member.declaringClass.kotlin.declaredMemberProperties.forEach { kProperty ->
            if (kProperty.javaGetter == this.member || (kProperty as? KMutableProperty1)?.javaSetter == this.member) {
                val byAnnotation = this.member.isRequiredByAnnotation()
                val byNullability = kProperty.isRequiredByNullability()
                return requiredAnnotationOrNullability(byAnnotation, byNullability)
            }
        }
        return null
    }

    // Is the member method a regular method of the data class or
    private fun Method.getRequiredMarkerFromAccessorLikeMethod(): Boolean? = this.kotlinFunction?.let { method ->
        val byAnnotation = this.isRequiredByAnnotation()
        return when {
            method.isGetterLike() -> requiredAnnotationOrNullability(byAnnotation, method.returnType.isRequired())
            method.isSetterLike() -> requiredAnnotationOrNullability(byAnnotation, method.isMethodParameterRequired(0))
            else -> null
        }
    }

    private fun KFunction<*>.isGetterLike(): Boolean = parameters.size == 1
    private fun KFunction<*>.isSetterLike(): Boolean = parameters.size == 2 && returnType == UNIT_TYPE

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

    private fun KFunction<*>.isConstructorParameterRequired(index: Int): Boolean {
        return isParameterRequired(index)
    }

    private fun KFunction<*>.isMethodParameterRequired(index: Int): Boolean {
        return isParameterRequired(index+1)
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

    companion object {
        val UNIT_TYPE: KType = Unit::class.createType()
    }
}
