package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.deser.SettableBeanProperty
import com.fasterxml.jackson.databind.deser.ValueInstantiator
import com.fasterxml.jackson.databind.deser.ValueInstantiators
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

class KotlinValueInstantiator(src: StdValueInstantiator) : StdValueInstantiator(src) {
    @Suppress("UNCHECKED_CAST")
    override fun createFromObjectWith(ctxt: DeserializationContext, props: Array<out SettableBeanProperty>, buffer: PropertyValueBuffer): Any? {
        val callable = when (_withArgsCreator) {
            is AnnotatedConstructor -> (_withArgsCreator.annotated as Constructor<Any>).kotlinFunction
            is AnnotatedMethod -> (_withArgsCreator.annotated as Method).kotlinFunction
            else -> throw IllegalStateException("Expected a construtor or method to create a Kotlin object, instead found ${_withArgsCreator.annotated.javaClass.name}")
        } ?: return super.createFromObjectWith(ctxt, props, buffer) // we cannot reflect this method so do the default Java-ish behavior
        callable.isAccessible = true

        val jsonParmValueList = buffer.getParameters(props) // properties in order, null for missing or actual nulled parameters

        // quick short circuit for special handling for no null checks needed and no optional parameters
        if (jsonParmValueList.none { it == null } && callable.parameters.none { it.isOptional }) {
            return super.createFromObjectWith(ctxt, jsonParmValueList)
        }

        val callableParametersByName = hashMapOf<KParameter, Any?>()

        callable.parameters.forEachIndexed { idx, paramDef ->
            if (paramDef.kind == KParameter.Kind.INSTANCE || paramDef.kind == KParameter.Kind.EXTENSION_RECEIVER) {
                // we shouldn't have an instance or receiver parameter and if we do, just go with default Java-ish behavior
                return super.createFromObjectWith(ctxt, jsonParmValueList)
            } else {
                val jsonProp = props.get(idx)
                val isMissing = !buffer.hasParameter(jsonProp)
                val paramVal = jsonParmValueList.get(idx)

                if (isMissing) {
                    if (paramDef.isOptional) {
                        // this is ok, optional parameter not resolved will have default parameter value of method
                    } else if (paramVal == null) {
                        if (paramDef.type.isMarkedNullable) {
                            // null value for nullable type, is ok
                            callableParametersByName.put(paramDef, null)
                        } else {
                            // missing value coming in as null for non-nullable type
                            throw JsonMappingException(null, "Instantiation of " + this.getValueTypeDesc() + " value failed for JSON property ${jsonProp.name} due to missing (therefore NULL) value for creator parameter ${paramDef.name} which is a non-nullable type")
                        }
                    } else {
                        // default value for datatype for non nullable type, is ok
                        callableParametersByName.put(paramDef, paramVal)
                    }
                } else {
                    if (paramVal == null && !paramDef.type.isMarkedNullable) {
                        // value coming in as null for non-nullable type
                        throw JsonMappingException(null, "Instantiation of " + this.getValueTypeDesc() + " value failed for JSON property ${jsonProp.name} due to NULL value for creator parameter ${paramDef.name} which is a non-nullable type")
                    } else {
                        // value present, and can be set
                        callableParametersByName.put(paramDef, paramVal)
                    }
                }
            }
        }


        return if (callableParametersByName.size == jsonParmValueList.size) {
            // we didn't do anything special with default parameters, do a normal call
            super.createFromObjectWith(ctxt, jsonParmValueList)
        } else {
            callable.isAccessible = true
            callable.callBy(callableParametersByName)
        }

    }

    override fun createFromObjectWith(ctxt: DeserializationContext, args: Array<out Any>): Any {
        return super.createFromObjectWith(ctxt, args)
    }

}

class KotlinInstantiators : ValueInstantiators {
    override fun findValueInstantiator(deserConfig: DeserializationConfig, beanDescriptor: BeanDescription, defaultInstantiator: ValueInstantiator): ValueInstantiator {
        return if (beanDescriptor.beanClass.isKotlinClass()) {
            if (defaultInstantiator is StdValueInstantiator) {
                KotlinValueInstantiator(defaultInstantiator)
            } else {
                // TODO: return defaultInstantiator and let default method parameters and nullability go unused?  or die with exception:
                throw IllegalStateException("KotlinValueInstantiator requires that the default ValueInstantiator is StdValueInstantiator")
            }
        } else {
            defaultInstantiator
        }
    }
}