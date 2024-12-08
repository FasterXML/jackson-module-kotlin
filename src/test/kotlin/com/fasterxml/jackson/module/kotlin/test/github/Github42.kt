package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestGithub42_FailOnNullForPrimitives {

    data class OptionalIntRequiredBoolean(val optInt: Int = -1, val reqBool: Boolean)

    val mapper = jacksonObjectMapper().enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

    @Test fun `optional primitive parameter defaulted if not in json when FAIL_ON_NULL_FOR_PRIMITIVES is true`() {
        val actualObj: OptionalIntRequiredBoolean = mapper.readValue("""
        {
            "reqBool": false
        }
        """)

        assertEquals(OptionalIntRequiredBoolean(reqBool = false), actualObj)
    }

    @Test fun `Exception thrown if required primitive parameter not in json when FAIL_ON_NULL_FOR_PRIMITIVES is true`() {
        assertThrows<JsonMappingException> {
            mapper.readValue<OptionalIntRequiredBoolean>("""{"optInt": 2}""")
        }
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
