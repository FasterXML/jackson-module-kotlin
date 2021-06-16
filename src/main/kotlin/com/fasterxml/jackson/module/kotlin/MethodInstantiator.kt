package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.Instantiator.Companion.INT_PRIMITIVE_CLASS
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

internal class MethodInstantiator<T>(
    kFunction: KFunction<T>,
    private val method: Method,
    private val instance: Any,
    companionAccessible: Boolean
) : Instantiator<T> {
    override val hasInstanceParameter: Boolean = true
    override val valueParameters: List<KParameter> = kFunction.valueParameters
    private val accessible: Boolean = companionAccessible && method.isAccessible
    private val bucketGenerator = BucketGenerator(valueParameters)

    init {
        method.isAccessible = true
    }

    // This initialization process is heavy and will not be done until it is needed.
    private val localMethod: Method by lazy {
        val instanceClazz = instance::class.java

        val argumentTypes = arrayOf(
            instanceClazz,
            *method.parameterTypes,
            *Array(bucketGenerator.maskSize) { INT_PRIMITIVE_CLASS },
            Object::class.java
        )

        SpreadWrapper.getDeclaredMethod(instanceClazz, "${method.name}\$default", argumentTypes)
            .apply { isAccessible = true }
    }
    private val originalDefaultValues: Array<Any?> by lazy {
        // argument size = parameterSize + maskSize + instanceSize(= 1) + markerSize(= 1)
        Array<Any?>(valueParameters.size + bucketGenerator.maskSize + 2) { null }.apply {
            this[0] = instance
        }
    }

    override fun checkAccessibility(ctxt: DeserializationContext) {
        if ((!accessible && ctxt.config.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) ||
            (accessible && ctxt.config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS))) {
            return
        }

        throw IllegalAccessException("Cannot access to Method, instead found ${method.name}")
    }

    override fun generateBucket() = bucketGenerator.generate()

    @Suppress("UNCHECKED_CAST")
    override fun callBy(bucket: ArgumentBucket) = when (bucket.isFullInitialized()) {
        true -> SpreadWrapper.invoke(method, instance, bucket.values)
        false -> {
            // When calling a method defined in companion object with default arguments,
            // the arguments are in the order of [instance, *args, *masks, null].
            // Since ArgumentBucket.getValuesOnDefault returns [*args, *masks, null],
            // it should be repacked into an array including instance.
            val values = originalDefaultValues.clone().apply {
                bucket.getValuesOnDefault().forEachIndexed { index, value ->
                    this[index + 1] = value
                }
            }
            SpreadWrapper.invoke(localMethod, null, values)
        }
    } as T
}
