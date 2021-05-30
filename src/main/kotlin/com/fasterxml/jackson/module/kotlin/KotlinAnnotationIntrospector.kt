package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.*
import com.fasterxml.jackson.databind.jsontype.NamedType
import kotlinx.metadata.*
import kotlinx.metadata.jvm.fieldSignature
import kotlinx.metadata.jvm.getterSignature
import kotlinx.metadata.jvm.setterSignature
import kotlinx.metadata.jvm.signature
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction


internal class KotlinAnnotationIntrospector(private val context: Module.SetupContext,
                                            private val cache: ReflectionCache,
                                            private val nullToEmptyCollection: Boolean,
                                            private val nullToEmptyMap: Boolean,
                                            private val nullIsSameAsDefault: Boolean) : NopAnnotationIntrospector() {

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

    override fun findCreatorAnnotation(config: MapperConfig<*>, a: Annotated): JsonCreator.Mode? {

        // TODO: possible work around for JsonValue class that requires the class constructor to have the JsonCreator(Mode.DELEGATED) set?
        // since we infer the creator at times for these methods, the wrong mode could be implied.

        // findCreatorBinding used to be a clearer way to set this, but we need to set the mode here to disambugiate the intent of the constructor
        return super.findCreatorAnnotation(config, a)
    }

    /**
     * Subclasses can be detected automatically for sealed classes, since all possible subclasses are known
     * at compile-time to Kotlin. This makes [com.fasterxml.jackson.annotation.JsonSubTypes] redundant.
     */
    override fun findSubtypes(a: Annotated): MutableList<NamedType>? = a.toKmClassOrNull()?.let { kmClass ->
        kmClass.sealedSubclasses
            .map { NamedType(it.toJavaClass()) }
            .toMutableList()
            .ifEmpty { null }
    }

    private fun AnnotatedField.hasRequiredMarker(): Boolean? {
        val field = member as Field
        val byAnnotation = field.isRequiredByAnnotation()
        val byNullability = field.declaringClass.toKmClassOrNull()?.let { kmClass ->
            kmClass.properties.find { field.name == it.fieldSignature?.name }?.returnType?.isRequired()
        }

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
    // nullability can be determined from the returnType of KmProperty.
    private fun KmProperty.isRequiredByNullability(): Boolean = !Flag.Type.IS_NULLABLE(returnType.flags)
    private fun KmType.isRequired() = !Flag.Type.IS_NULLABLE(flags)

    private fun AnnotatedMethod.hasRequiredMarker(): Boolean? = this.getRequiredMarkerFromCorrespondingAccessor()
        ?: this.member.getRequiredMarkerFromAccessorLikeMethod()

    private fun AnnotatedMethod.getRequiredMarkerFromCorrespondingAccessor(): Boolean? {
        member.declaringClass.toKmClassOrNull()?.let { kmClass ->
            kmClass.properties.forEach { kmProperty ->
                if (member.isAccessorOf(kmProperty)) {
                    val byAnnotation = this.member.isRequiredByAnnotation()
                    val byNullability = kmProperty.isRequiredByNullability()
                    return requiredAnnotationOrNullability(byAnnotation, byNullability)
                }
            }
        }
        return null
    }

    private fun Method.isAccessorOf(kmProperty: KmProperty): Boolean =
        kmProperty.getterSignature?.name == name || kmProperty.setterSignature?.name == name

    // Is the member method a regular method of the data class or
    private fun Method.getRequiredMarkerFromAccessorLikeMethod(): Boolean? =
        this.declaringClass.toKmClassOrNull()?.let { kmClass ->
            val byAnnotation = this.isRequiredByAnnotation()
            kmClass.functions.forEach { kmFunction ->
                if (kmFunction.signature?.name == name) {
                    val byNullability = when {
                        kmFunction.isGetterLike() -> kmFunction.returnType.isRequired()
                        kmFunction.isSetterLike() -> isMethodParameterRequired(kmFunction.valueParameters[0], this.parameterTypes[0])
                        else -> return@let null
                    }
                    return@let requiredAnnotationOrNullability(byAnnotation, byNullability)
                }
            }
            null
        }

    private fun KmFunction.isGetterLike(): Boolean = valueParameters.size == 0
    // As an edge case, there is a pattern where the return type is Unit?, but it is not considered.
    private fun KmFunction.isSetterLike(): Boolean =
        valueParameters.size == 1 && returnType.classifier == UNIT_CLASSIFIER

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
    private fun isMethodParameterRequired(param: KmValueParameter, type: Class<*>): Boolean {
        val isRequired = Flag.Type.IS_NULLABLE(param.type!!.flags)
        val isOptional = Flag.ValueParameter.DECLARES_DEFAULT_VALUE(param.flags)
        val isPrimitive = type.isPrimitive

        return !isRequired && !isOptional &&
                !(isPrimitive && !context.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES))
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

    companion object {
        val UNIT_CLASSIFIER: KmClassifier = KmClassifier.Class("kotlin/Unit")
    }
}
