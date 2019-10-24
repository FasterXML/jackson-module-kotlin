package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import kotlin.test.assertEquals


class FailOnNullForPrimitivesTests {

    data class OptionalIntRequiredBoolean(val optInt: Int = -1, val reqBool: Boolean)

    val mapper = jacksonMapperBuilder().enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build()

    @Rule
    @JvmField
    var thrown: ExpectedException = ExpectedException.none()

    @Test fun `optional primitive parameter defaulted if not in json when FAIL_ON_NULL_FOR_PRIMITIVES is true`() {
        val actualObj: OptionalIntRequiredBoolean = mapper.readValue("""
        {
            "reqBool": false
        }
        """)

        assertEquals(OptionalIntRequiredBoolean(reqBool = false), actualObj)
    }

    @Test fun `Exception thrown if required primitive parameter not in json when FAIL_ON_NULL_FOR_PRIMITIVES is true`() {
        thrown.expect(JsonMappingException::class.java)

        mapper.readValue<OptionalIntRequiredBoolean>("""
        {"optInt": 2}
        """)
    }

    @Test fun `optional parameter has json value if provided when FAIL_ON_NULL_FOR_PRIMITIVES is true`() {
        val actualObj: OptionalIntRequiredBoolean = mapper.readValue("""
        {
            "optInt": 3,
            "reqBool": true
        }
        """)

        assertEquals(OptionalIntRequiredBoolean(3, true), actualObj)
    }
}
