package com.fasterxml.jackson.module.kotlin

import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MissingKotlinParameterExceptionTest {
    @Test
    fun jdkSerializabilityTest() {
        val param = ::MissingKotlinParameterException.parameters.first()
        val ex = MissingKotlinParameterException(param, null, "test")

        val serialized = jdkSerialize(ex)
        val deserialized = jdkDeserialize<MissingKotlinParameterException>(serialized)

        assertNotNull(deserialized)
        // see comment at MissingKotlinParameterException
        assertNull(deserialized.parameter)
    }
}
