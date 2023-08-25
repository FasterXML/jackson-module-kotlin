package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.deser.SettableBeanProperty
import com.fasterxml.jackson.databind.deser.ValueInstantiator
import com.fasterxml.jackson.databind.deser.ValueInstantiators
import com.fasterxml.jackson.databind.deser.impl.NullsAsEmptyProvider
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator
import com.fasterxml.jackson.databind.exc.MismatchedInputException
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

    private fun SettableBeanProperty.skipNulls(): Boolean =
        nullIsSameAsDefault || (getAnnotation(JsonSetter::class.java)?.nulls == Nulls.SKIP)

    override fun createFromObjectWith(
        ctxt: DeserializationContext,
        props: Array<out SettableBeanProperty>,
        buffer: PropertyValueBuffer
    ): Any? {
        val valueCreator: ValueCreator<*> = cache.valueCreatorFromJava(_withArgsCreator)
            ?: return super.createFromObjectWith(ctxt, props, buffer)

        val propCount: Int
        var numCallableParameters: Int
        val callableParameters: Array<KParameter?>
        val jsonParamValueList: Array<Any?>

        if (valueCreator is MethodValueCreator) {
            propCount = props.size + 1
            numCallableParameters = 1
            callableParameters = arrayOfNulls<KParameter>(propCount)
                .apply { this[0] = valueCreator.instanceParameter }
            jsonParamValueList = arrayOfNulls<Any>(propCount)
                .apply { this[0] = valueCreator.companionObjectInstance }
        } else {
            propCount = props.size
            numCallableParameters = 0
            callableParameters = arrayOfNulls(propCount)
            jsonParamValueList = arrayOfNulls(propCount)
        }

        valueCreator.valueParameters.forEachIndexed { idx, paramDef ->
            val jsonProp = props[idx]
            val isMissing = !buffer.hasParameter(jsonProp)

            if (isMissing && paramDef.isOptional) {
                return@forEachIndexed
            }

            val paramType = paramDef.type
            var paramVal = if (!isMissing || paramDef.isPrimitive() || jsonProp.hasInjectableValueId()) {
                val tempParamVal = buffer.getParameter(jsonProp)
                if (tempParamVal == null && jsonProp.skipNulls() && paramDef.isOptional) {
                    return@forEachIndexed
                }
                tempParamVal
            } else {
                if(paramType.isMarkedNullable) {
                    // do not try to create any object if it is nullable and the value is missing
                    null
                } else {
                    // to get suitable "missing" value provided by deserializer
                    jsonProp.valueDeserializer?.getAbsentValue(ctxt)
                }
            }

            val propType = jsonProp.type

            if (paramVal == null) {
                if (propType.requireEmptyValue()) {
                    paramVal = NullsAsEmptyProvider(jsonProp.valueDeserializer).getNullValue(ctxt)
                } else {
                    val isMissingAndRequired = isMissing && jsonProp.isRequired

                    // Since #310 reported that the calculation cost is high, isGenericTypeVar is determined last.
                    if (isMissingAndRequired || (!paramType.isMarkedNullable && !paramType.isGenericTypeVar())) {
                        throw MismatchedInputException.from(
                            ctxt.parser,
                            propType,
                            "Instantiation of $valueTypeDesc value failed for JSON property ${jsonProp.name} " +
                                    "due to missing (therefore NULL) value for creator parameter ${paramDef.name} " +
                                    "which is a non-nullable type"
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
                    throw MismatchedInputException.from(
                        ctxt.parser,
                        propType,
                        "Instantiation of $itemType $paramTypeStr failed for JSON property ${jsonProp.name} due to null value in a $paramTypeStr that does not allow null values"
                    ).wrapWithPath(this.valueClass, jsonProp.name)
                }
            }

            jsonParamValueList[numCallableParameters] = paramVal
            callableParameters[numCallableParameters] = paramDef
            numCallableParameters++
        }

        return if (numCallableParameters == jsonParamValueList.size && valueCreator is ConstructorValueCreator) {
            // we didn't do anything special with default parameters, do a normal call
            super.createFromObjectWith(ctxt, jsonParamValueList)
        } else {
            valueCreator.checkAccessibility(ctxt)

            val callableParametersByName = linkedMapOf<KParameter, Any?>()
            callableParameters.mapIndexed { idx, paramDef ->
                if (paramDef != null) {
                    callableParametersByName[paramDef] = jsonParamValueList[idx]
                }
            }
            valueCreator.callBy(callableParametersByName)
        }

    }

    private fun KParameter.isPrimitive(): Boolean {
        return when (val javaType = type.javaType) {
            is Class<*> -> javaType.isPrimitive
            else -> false
        }
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
