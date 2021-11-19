package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.DeserializationContext
import kotlin.reflect.KParameter

internal interface Instantiator<T> {
    val hasInstanceParameter: Boolean

    /**
     * ValueParameters of the KFunction to be called.
     */
    val valueParameters: List<KParameter>

    /**
     * Checking process to see if access from context is possible.
     * @throws  IllegalAccessException
     */
    fun checkAccessibility(ctxt: DeserializationContext)

    /**
     * The process of getting the target bucket to set the value.
     */
    fun generateBucket(): ArgumentBucket

    /**
     * Function call from bucket.
     * If there are uninitialized arguments, the call is made using the default function.
     */
    fun callBy(bucket: ArgumentBucket): T

    companion object {
        val INT_PRIMITIVE_CLASS: Class<Int> = Int::class.javaPrimitiveType!!
    }
}
