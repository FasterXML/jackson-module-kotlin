package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.SettableBeanProperty
import com.fasterxml.jackson.databind.deser.ValueInstantiator
import com.fasterxml.jackson.databind.deser.ValueInstantiators
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator
import java.lang.reflect.TypeVariable
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.jvm.javaType

internal class KotlinValueInstantiator(
    src: StdValueInstantiator,
    private val cache: ReflectionCache,
    private val nullToEmptyCollection: Boolean,
    private val nullToEmptyMap: Boolean,
    private val nullIsSameAsDefault: Boolean,
    private val strictNullChecks: Boolean
) : StdValueInstantiator(src) {
    private fun JavaType.requireEmptyValue() =
        (nullToEmptyCollection && this.isCollectionLikeType) || (nullToEmptyMap && this.isMapLikeType)

    private fun KType.isGenericTypeVar() = javaType is TypeVariable<*>

    private fun List<KTypeProjection>.markedNonNullAt(index: Int) = getOrNull(index)?.type?.isMarkedNullable == false

    // If the argument is a value class that wraps nullable and non-null,
    // and the input is explicit null, the value class is instantiated with null as input.
    private fun requireValueClassSpecialNullValue(
        isNullableParam: Boolean,
        valueDeserializer: JsonDeserializer<*>?
    ): Boolean = !isNullableParam &&
            valueDeserializer is WrapsNullableValueClassDeserializer<*> &&
            valueDeserializer.handledType().kotlin.wrapsNullable()

    private fun SettableBeanProperty.skipNulls(): Boolean =
        nullIsSameAsDefault || (metadata.valueNulls == Nulls.SKIP)

    override fun createFromObjectWith(
        ctxt: DeserializationContext,
        props: Array<out SettableBeanProperty>,
        buffer: PropertyValueBuffer
    ): Any? {
        val valueCreator: ValueCreator<*> = cache.valueCreatorFromJava(_withArgsCreator)
            ?: return super.createFromObjectWith(ctxt, props, buffer)

        val bucket = valueCreator.generateBucket()

        valueCreator.valueParameters.forEachIndexed { idx, paramDef ->
            val jsonProp = props[idx]
            val isMissing = !buffer.hasParameter(jsonProp)
            val valueDeserializer: JsonDeserializer<*>? by lazy { jsonProp.valueDeserializer }

            val paramType = paramDef.type
            var paramVal = if (!isMissing || jsonProp.hasInjectableValueId()) {
               buffer.getParameter(jsonProp) ?: run {
                   // Deserializer.getNullValue could not be used because there is no way to get and parse parameters
                   // from the BeanDescription and using AnnotationIntrospector would override user customization.
                   if (requireValueClassSpecialNullValue(paramDef.type.isMarkedNullable, valueDeserializer)) {
                       (valueDeserializer as WrapsNullableValueClassDeserializer<*>).boxedNullValue?.let { return@run it }
                   }

                   if (jsonProp.skipNulls() && paramDef.isOptional) return@forEachIndexed

                   null
               }
            } else {
                when {
                    paramDef.isOptional || paramDef.isVararg -> return@forEachIndexed
                    // do not try to create any object if it is nullable and the value is missing
                    paramType.isMarkedNullable -> null
                    // Primitive types always try to get from a buffer, considering several settings
                    jsonProp.type.isPrimitive -> buffer.getParameter(jsonProp)
                    // to get suitable "missing" value provided by deserializer
                    else -> valueDeserializer?.getAbsentValue(ctxt)
                }
            }

            val propType = jsonProp.type

            if (paramVal == null) {
                if (propType.requireEmptyValue()) {
                    paramVal = valueDeserializer!!.getEmptyValue(ctxt)
                } else {
                    val isMissingAndRequired = isMissing && jsonProp.isRequired

                    // Since #310 reported that the calculation cost is high, isGenericTypeVar is determined last.
                    if (isMissingAndRequired || (!paramType.isMarkedNullable && !paramType.isGenericTypeVar())) {
                        throw MissingKotlinParameterException(
                            parameter = paramDef,
                            processor = ctxt.parser,
                            msg = "Instantiation of ${this.valueTypeDesc} value failed for JSON property ${jsonProp.name} due to missing (therefore NULL) value for creator parameter ${paramDef.name} which is a non-nullable type"
                        ).wrapWithPath(this.valueClass, jsonProp.name)
                    }
                }
            } else if (strictNullChecks) {
                val arguments = paramType.arguments

                var paramTypeStr: String? = null
                var itemType: KType? = null

                if (propType.isCollectionLikeType && arguments.markedNonNullAt(0) && (paramVal as Collection<*>).any { it == null }) {
                    paramTypeStr = "collection"
                    itemType = arguments[0].type
                }

                if (propType.isMapLikeType && arguments.markedNonNullAt(1) && (paramVal as Map<*, *>).any { it.value == null }) {
                    paramTypeStr = "map"
                    itemType = arguments[1].type
                }

                if (propType.isArrayType && arguments.markedNonNullAt(0) && (paramVal as Array<*>).any { it == null }) {
                    paramTypeStr = "array"
                    itemType = arguments[0].type
                }

                if (paramTypeStr != null && itemType != null) {
                    throw MissingKotlinParameterException(
                        parameter = paramDef,
                        processor = ctxt.parser,
                        msg = "Instantiation of $itemType $paramType failed for JSON property ${jsonProp.name} due to null value in a $paramType that does not allow null values"
                    ).wrapWithPath(this.valueClass, jsonProp.name)
                }
            }

            bucket[paramDef] = paramVal
        }

        valueCreator.checkAccessibility(ctxt)

        return valueCreator.callBy(bucket)
    }

    private fun SettableBeanProperty.hasInjectableValueId(): Boolean = injectableValueId != null
}

internal class KotlinInstantiators(
    private val cache: ReflectionCache,
    private val nullToEmptyCollection: Boolean,
    private val nullToEmptyMap: Boolean,
    private val nullIsSameAsDefault: Boolean,
    private val strictNullChecks: Boolean
) : ValueInstantiators {
    override fun findValueInstantiator(
        deserConfig: DeserializationConfig,
        beanDescriptor: BeanDescription,
        defaultInstantiator: ValueInstantiator
    ): ValueInstantiator {
        return if (beanDescriptor.beanClass.isKotlinClass()) {
            if (defaultInstantiator::class == StdValueInstantiator::class) {
                KotlinValueInstantiator(
                    defaultInstantiator as StdValueInstantiator,
                    cache,
                    nullToEmptyCollection,
                    nullToEmptyMap,
                    nullIsSameAsDefault,
                    strictNullChecks
                )
            } else {
                // TODO: return defaultInstantiator and let default method parameters and nullability go unused?  or die with exception:
                throw IllegalStateException("KotlinValueInstantiator requires that the default ValueInstantiator is StdValueInstantiator")
            }
        } else {
            defaultInstantiator
        }
    }
}
