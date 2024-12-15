package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class FailNullForPrimitiveTest {
    val mapper = jacksonObjectMapper()
        .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

    data class NoDefaultValue(
        val foo: Int,
        val bar: Int?
    )

    @Test
    fun noDefaultValueTest() {
        // If no default value is set, it will fail if undefined or null is entered
        assertThrows(MismatchedInputException::class.java) {
            mapper.readValue<NoDefaultValue>("{}")
        }

        assertThrows(MismatchedInputException::class.java) {
            mapper.readValue<NoDefaultValue>("""{"foo":null}""")
        }

        assertEquals(NoDefaultValue(0, null), mapper.readValue<NoDefaultValue>("""{"foo":0}"""))
    }

    data class HasDefaultValue(
        val foo: Int = -1,
        val bar: Int? = -1
    )

    @Test
    fun hasDefaultValueTest() {
        // If a default value is set, an input of undefined will succeed, but null will fail
        assertEquals(HasDefaultValue(-1, -1), mapper.readValue<HasDefaultValue>("{}"))

        assertThrows(MismatchedInputException::class.java) {
            mapper.readValue<HasDefaultValue>("""{"foo":null}""")
        }

        assertEquals(HasDefaultValue(0, null), mapper.readValue<HasDefaultValue>("""{"foo":0, "bar":null}"""))
    }
}
