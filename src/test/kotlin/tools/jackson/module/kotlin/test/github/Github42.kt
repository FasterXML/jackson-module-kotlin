package tools.jackson.module.kotlin.test.github

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.DatabindException
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestGithub42_FailOnNullForPrimitives {

    data class OptionalIntRequiredBoolean(val optInt: Int = -1, val reqBool: Boolean)

    val mapper = jacksonMapperBuilder().enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build()

    @Test fun `optional primitive parameter defaulted if not in json when FAIL_ON_NULL_FOR_PRIMITIVES is true`() {
        val actualObj: OptionalIntRequiredBoolean = mapper.readValue("""
        {
            "reqBool": false
        }
        """)

        assertEquals(OptionalIntRequiredBoolean(reqBool = false), actualObj)
    }

    @Test fun `Exception thrown if required primitive parameter not in json when FAIL_ON_NULL_FOR_PRIMITIVES is true`() {
        assertThrows<DatabindException> {
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
