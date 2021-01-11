package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.deser.SettableBeanProperty
import com.fasterxml.jackson.databind.deser.ValueInstantiator
import com.fasterxml.jackson.databind.deser.ValueInstantiators
import com.fasterxml.jackson.databind.deser.impl.NullsAsEmptyProvider
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.TypeVariable
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

internal class KotlinValueInstantiator(
    src: StdValueInstantiator,
    private val cache: ReflectionCache,
    private val nullToEmptyCollection: Boolean,
    private val nullToEmptyMap: Boolean,
    private val nullIsSameAsDefault: Boolean,
    private val strictNullChecks: Boolean
) : StdValueInstantiator(src) {
    @Suppress("UNCHECKED_CAST")
    override fun createFromObjectWith(
        ctxt: DeserializationContext,
        props: Array<out SettableBeanProperty>,
        buffer: PropertyValueBuffer
    ): Any? {
        val callable = when (_withArgsCreator) {
            is AnnotatedConstructor -> cache.kotlinFromJava(_withArgsCreator.annotated as Constructor<Any>)
            is AnnotatedMethod -> cache.kotlinFromJava(_withArgsCreator.annotated as Method)
            else -> throw IllegalStateException("Expected a constructor or method to create a Kotlin object, instead found ${_withArgsCreator.annotated.javaClass.name}")
        } ?: return super.createFromObjectWith(
            ctxt,
            props,
            buffer
        ) // we cannot reflect this method so do the default Java-ish behavior

        if (callable.extensionReceiverParameter != null) {
            // we shouldn't have an instance or receiver parameter and if we do, just go with default Java-ish behavior
            return super.createFromObjectWith(ctxt, props, buffer)
        }

        val propCount = props.size + if (callable.instanceParameter != null) 1 else 0

        var numCallableParameters = 0
        val callableParameters = arrayOfNulls<KParameter>(propCount)
        val jsonParamValueList = arrayOfNulls<Any>(propCount)

        if (callable.instanceParameter != null) {
            val possibleCompanion = callable.instanceParameter!!.type.erasedType().kotlin

            if (!possibleCompanion.isCompanion) {
                // abort, we have some unknown case here
                return super.createFromObjectWith(ctxt, props, buffer)
            }

            // TODO: cache this lookup since the exception throwing/catching can be expensive
            jsonParamValueList[numCallableParameters] = try {
                possibleCompanion.objectInstance
            } catch (ex: IllegalAccessException) {
                // fallback for when an odd access exception happens through Kotlin reflection
                val companionField = possibleCompanion.java.enclosingClass.fields.firstOrNull { it.name == "Companion" }
                        ?: throw ex
                val accessible = companionField.isAccessible
                if ((!accessible && ctxt.config.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) ||
                    (accessible && ctxt.config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS))
                ) {
                    companionField.isAccessible = true
                }
                companionField.get(null) ?: throw ex
            }

            callableParameters[numCallableParameters] = callable.instanceParameter
            numCallableParameters++
        }

        callable.valueParameters.forEachIndexed { idx, paramDef ->
            val jsonProp = props[idx]
            val isMissing = !buffer.hasParameter(jsonProp)

            if (isMissing && paramDef.isOptional) {
                return@forEachIndexed
            }

            var paramVal = if (!isMissing || paramDef.isPrimitive() || jsonProp.hasInjectableValueId()) {
                val tempParamVal = buffer.getParameter(jsonProp)
                if (nullIsSameAsDefault && tempParamVal == null && paramDef.isOptional) {
                    return@forEachIndexed
                }
                tempParamVal
            } else {
                // trying to get suitable "missing" value provided by deserializer
                jsonProp.valueDeserializer?.getNullValue(ctxt)
            }

            if (paramVal == null && ((nullToEmptyCollection && jsonProp.type.isCollectionLikeType) || (nullToEmptyMap && jsonProp.type.isMapLikeType))) {
                paramVal = NullsAsEmptyProvider(jsonProp.valueDeserializer).getNullValue(ctxt)
            }

            val isGenericTypeVar = paramDef.type.javaType is TypeVariable<*>
            val isMissingAndRequired = paramVal == null && isMissing && jsonProp.isRequired
            if (isMissingAndRequired ||
                (!isGenericTypeVar && paramVal == null && !paramDef.type.isMarkedNullable)) {
                throw MissingKotlinParameterException(
                    parameter = paramDef,
                    processor = ctxt.parser,
                    msg = "Instantiation of ${this.valueTypeDesc} value failed for JSON property ${jsonProp.name} due to missing (therefore NULL) value for creator parameter ${paramDef.name} which is a non-nullable type"
                ).wrapWithPath(this.valueClass, jsonProp.name)
            }

            if (strictNullChecks && paramVal != null) {
                var paramType: String? = null
                var itemType: KType? = null
                if (jsonProp.type.isCollectionLikeType && paramDef.type.arguments.getOrNull(0)?.type?.isMarkedNullable == false && (paramVal as Collection<*>).any { it == null }) {
                    paramType = "collection"
                    itemType = paramDef.type.arguments[0].type
                }

                if (jsonProp.type.isMapLikeType && paramDef.type.arguments.getOrNull(1)?.type?.isMarkedNullable == false && (paramVal as Map<*, *>).any { it.value == null }) {
                    paramType = "map"
                    itemType = paramDef.type.arguments[1].type
                }

                if (jsonProp.type.isArrayType && paramDef.type.arguments.getOrNull(0)?.type?.isMarkedNullable == false && (paramVal as Array<*>).any { it == null }) {
                    paramType = "array"
                    itemType = paramDef.type.arguments[0].type
                }

                if (paramType != null && itemType != null) {
                    throw MissingKotlinParameterException(
                        parameter = paramDef,
                        processor = ctxt.parser,
                        msg = "Instantiation of $itemType $paramType failed for JSON property ${jsonProp.name} due to null value in a $paramType that does not allow null values"
                    ).wrapWithPath(this.valueClass, jsonProp.name)
                }
            }

            jsonParamValueList[numCallableParameters] = paramVal
            callableParameters[numCallableParameters] = paramDef
            numCallableParameters++
        }

        return if (numCallableParameters == jsonParamValueList.size && callable.instanceParameter == null) {
            // we didn't do anything special with default parameters, do a normal call
            super.createFromObjectWith(ctxt, jsonParamValueList)
        } else {
            val accessible = callable.isAccessible
            if ((!accessible && ctxt.config.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) ||
                (accessible && ctxt.config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS))
            ) {
                callable.isAccessible = true
            }
            ArgumentBucket(callable.parameters).apply {
                callableParameters.forEachIndexed { idx, paramDef ->
                    if (paramDef != null) {
                        this[paramDef] = jsonParamValueList[idx]
                    }
                }
            }.let {
                if (it.isFullInitialized())
                    SpreadWrapper.call(callable, it.valueArray)
                else
                    callable.callBy(it)
            }
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
            if (defaultInstantiator is StdValueInstantiator) {
                KotlinValueInstantiator(defaultInstantiator, cache, nullToEmptyCollection, nullToEmptyMap, nullIsSameAsDefault, strictNullChecks)
            } else {
                // TODO: return defaultInstantiator and let default method parameters and nullability go unused?  or die with exception:
                throw IllegalStateException("KotlinValueInstantiator requires that the default ValueInstantiator is StdValueInstantiator")
            }
        } else {
            defaultInstantiator
        }
    }
}
