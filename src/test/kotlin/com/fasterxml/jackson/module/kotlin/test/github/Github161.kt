package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.fail

class TestGithub161 {
    data class Foo(
        val foo: Int,
        val bar: Int
    )

    @Test
    fun testPrimitiveBeingZeroed() {
        val json = """{"foo":17}"""
        val objectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        try {
            val foo = objectMapper.readValue(json, Foo::class.java)
            fail("Expected an error on the missing primitive value")
        } catch (ex: MismatchedInputException) {
            // success
        }
    }
}