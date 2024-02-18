package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.introspect.*
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.databind.util.Converter
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinProperty
import kotlin.time.Duration

internal class KotlinAnnotationIntrospector(
    private val context: Module.SetupContext,
    private val cache: ReflectionCache,
    private val nullToEmptyCollection: Boolean,
    private val nullToEmptyMap: Boolean,
    private val nullIsSameAsDefault: Boolean,
    private val useJavaDurationConversion: Boolean,
) : NopAnnotationIntrospector() {

    // TODO: implement nullIsSameAsDefault flag, which represents when TRUE that if something has a default value, it can be passed a null to default it
    //       this likely impacts this class to be accurate about what COULD be considered required

    override fun hasRequiredMarker(m: AnnotatedMember): Boolean? {
        val hasRequired = cache.javaMemberIsRequired(m) {
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
        return hasRequired
    }

    override fun findSerializationConverter(a: Annotated): Converter<*, *>? = when (a) {
        // Find a converter to handle the case where the getter returns an unboxed value from the value class.
        is AnnotatedMethod -> a.findValueClassReturnType()?.let {
            if (useJavaDurationConversion && it == Duration::class) {
                if (a.rawReturnType == Duration::class.java)
                    KotlinToJavaDurationConverter
                else
                    KotlinDurationValueToJavaDurationConverter
            } else {
                cache.getValueClassBoxConverter(a.rawReturnType, it)
            }
        }
        is AnnotatedClass -> lookupKotlinTypeConverter(a)
        else -> null
    }

    private fun lookupKotlinTypeConverter(a: AnnotatedClass) = when {
        Sequence::class.java.isAssignableFrom(a.rawType) -> SequenceToIteratorConverter(a.type)
        Duration::class.java == a.rawType -> KotlinToJavaDurationConverter.takeIf { useJavaDurationConversion }
        else -> null
    }

    // Perform proper serialization even if the value wrapped by the value class is null.
    // If value is a non-null object type, it must not be reboxing.
    override fun findNullSerializer(am: Annotated): JsonSerializer<*>? = (am as? AnnotatedMethod)
        ?.findValueClassReturnType()
        ?.takeIf { it.wrapsNullable() }
        ?.let { cache.getValueClassBoxConverter(am.rawReturnType, it).delegatingSerializer }

    override fun findDeserializationConverter(a: Annotated): Any? {
        if (!useJavaDurationConversion) return null

        return (a as? AnnotatedParameter)?.let { param ->
            val valueParameter = cache.findKotlinParameter(param) ?: return@let null

            if (valueParameter.type.classifier == Duration::class) {
                JavaToKotlinDurationConverter
            } else {
                null
            }
        }
    }

    /**
     * Subclasses can be detected automatically for sealed classes, since all possible subclasses are known
     * at compile-time to Kotlin. This makes [com.fasterxml.jackson.annotation.JsonSubTypes] redundant.
     */
    override fun findSubtypes(a: Annotated): MutableList<NamedType>? = a.rawType
        .takeIf { it.isKotlinClass() }
        ?.let { rawType ->
            rawType.kotlin.sealedSubclasses
                .map { NamedType(it.java) }
                .toMutableList()
                .ifEmpty { null }
        }

    private fun AnnotatedField.hasRequiredMarker(): Boolean? {
        val byAnnotation = (member as Field).isRequiredByAnnotation()
        val byNullability = (member as Field).kotlinProperty?.returnType?.isRequired()

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
    private fun Method.getRequiredMarkerFromAccessorLikeMethod(): Boolean? = cache.kotlinFromJava(this)?.let { func ->
        val byAnnotation = this.isRequiredByAnnotation()
        return when {
            func.isGetterLike() -> requiredAnnotationOrNullability(byAnnotation, func.returnType.isRequired())
            func.isSetterLike() -> requiredAnnotationOrNullability(byAnnotation, func.valueParameters[0].isRequired())
            else -> null
        }
    }

    private fun KFunction<*>.isGetterLike(): Boolean = parameters.size == 1
    private fun KFunction<*>.isSetterLike(): Boolean = parameters.size == 2 && returnType == UNIT_TYPE

    private fun AnnotatedParameter.hasRequiredMarker(): Boolean? {
        val byAnnotation = this.getAnnotation(JsonProperty::class.java)?.required
        val byNullability = cache.findKotlinParameter(this)?.isRequired()

        return requiredAnnotationOrNullability(byAnnotation, byNullability)
    }

    private fun AnnotatedMethod.findValueClassReturnType() = cache.findValueClassReturnType(this)

    private fun KParameter.isRequired(): Boolean {
        val paramType = type
        val isPrimitive = when (val javaType = paramType.javaType) {
            is Class<*> -> javaType.isPrimitive
            else -> false
        }

        return !paramType.isMarkedNullable && !isOptional && !isVararg &&
                !(isPrimitive && !context.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES))
    }

    private fun KType.isRequired(): Boolean = !isMarkedNullable

    companion object {
        val UNIT_TYPE: KType by lazy { Unit::class.createType() }
    }
}
