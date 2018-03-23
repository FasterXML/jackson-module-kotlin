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
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

internal class KotlinValueInstantiator(src: StdValueInstantiator, private val cache: ReflectionCache, private val nullToEmptyCollection: Boolean, private val nullToEmptyMap: Boolean) : StdValueInstantiator(src) {
    @Suppress("UNCHECKED_CAST")
    override fun createFromObjectWith(ctxt: DeserializationContext, props: Array<out SettableBeanProperty>, buffer: PropertyValueBuffer): Any? {
        val callable = when (_withArgsCreator) {
            is AnnotatedConstructor -> cache.kotlinFromJava(_withArgsCreator.annotated as Constructor<Any>)
            is AnnotatedMethod -> cache.kotlinFromJava(_withArgsCreator.annotated as Method)
            else -> throw IllegalStateException("Expected a constructor or method to create a Kotlin object, instead found ${_withArgsCreator.annotated.javaClass.name}")
        } ?: return super.createFromObjectWith(ctxt, props, buffer) // we cannot reflect this method so do the default Java-ish behavior

        var numCallableParameters = 0
        val callableParameters = arrayOfNulls<KParameter>(props.size)
        val jsonParamValueList = arrayOfNulls<Any>(props.size)

        callable.parameters.forEachIndexed { idx, paramDef ->
            if (paramDef.kind == KParameter.Kind.INSTANCE || paramDef.kind == KParameter.Kind.EXTENSION_RECEIVER) {
                // we shouldn't have an instance or receiver parameter and if we do, just go with default Java-ish behavior
                return super.createFromObjectWith(ctxt, props, buffer)
            }
            val jsonProp = props[idx]
            val isMissing = !buffer.hasParameter(jsonProp)

            if (isMissing && paramDef.isOptional) {
                return@forEachIndexed
            }

            var paramVal = if (!isMissing || paramDef.isPrimitive() || jsonProp.hasInjectableValueId()) {
                buffer.getParameter(jsonProp)
            } else {
                null
            }

            if (paramVal == null && ((nullToEmptyCollection && jsonProp.type.isCollectionLikeType) || (nullToEmptyMap && jsonProp.type.isMapLikeType))) {
                paramVal = NullsAsEmptyProvider(jsonProp.valueDeserializer).getNullValue(ctxt)
            }

            jsonParamValueList[idx] = paramVal

            if (paramVal == null && !paramDef.type.isMarkedNullable) {
                throw MissingKotlinParameterException(
                        parameter = paramDef,
                        processor = ctxt.parser,
                        msg = "Instantiation of ${this.valueTypeDesc} value failed for JSON property ${jsonProp.name} due to missing (therefore NULL) value for creator parameter ${paramDef.name} which is a non-nullable type"
                ).wrapWithPath(this.valueClass, jsonProp.name)
            }

            numCallableParameters++
            callableParameters[idx] = paramDef
        }

        return if (numCallableParameters == jsonParamValueList.size) {
            // we didn't do anything special with default parameters, do a normal call
            super.createFromObjectWith(ctxt, jsonParamValueList)
        } else {
            val accessible = callable.isAccessible
            if ((!accessible && ctxt.config.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) ||
                    (accessible && ctxt.config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS))) {
                callable.isAccessible = true
            }
            val callableParametersByName = linkedMapOf<KParameter, Any?>()
            callableParameters.mapIndexed { idx, paramDef ->
                if (paramDef != null) {
                    callableParametersByName[paramDef] = jsonParamValueList[idx]
                }
            }
            callable.callBy(callableParametersByName)
        }

    }

    override fun createFromObjectWith(ctxt: DeserializationContext, args: Array<out Any>): Any {
        return super.createFromObjectWith(ctxt, args)
    }

    private fun KParameter.isPrimitive(): Boolean {
        val javaType = type.javaType
        return when (javaType) {
            is Class<*> -> javaType.isPrimitive
            else -> false
        }
    }

    private fun SettableBeanProperty.hasInjectableValueId(): Boolean = injectableValueId != null
}

internal class KotlinInstantiators(private val cache: ReflectionCache, private val nullToEmptyCollection: Boolean, private val nullToEmptyMap: Boolean) : ValueInstantiators {
    override fun findValueInstantiator(deserConfig: DeserializationConfig, beanDescriptor: BeanDescription, defaultInstantiator: ValueInstantiator): ValueInstantiator {
        return if (beanDescriptor.beanClass.isKotlinClass()) {
            if (defaultInstantiator is StdValueInstantiator) {
                KotlinValueInstantiator(defaultInstantiator, cache, nullToEmptyCollection, nullToEmptyMap)
            } else {
                // TODO: return defaultInstantiator and let default method parameters and nullability go unused?  or die with exception:
                throw IllegalStateException("KotlinValueInstantiator requires that the default ValueInstantiator is StdValueInstantiator")
            }
        } else {
            defaultInstantiator
        }
    }
}
