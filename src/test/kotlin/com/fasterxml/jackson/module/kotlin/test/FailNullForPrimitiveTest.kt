package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class FailNullForPrimitiveTest {
    data class Dto(
        val foo: Int,
        val bar: Int?
    )

    @Test
    fun test() {
        val mapper = jacksonObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

        assertThrows(MismatchedInputException::class.java) {
            mapper.readValue<Dto>("{}")
        }

        assertThrows(MismatchedInputException::class.java) {
            mapper.readValue<Dto>("""{"foo":null}""")
        }

        assertEquals(Dto(0, null), mapper.readValue<Dto>("""{"foo":0}"""))
    }
}
