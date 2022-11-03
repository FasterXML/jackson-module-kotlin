package tools.jackson.module.kotlin.test.github

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JsonMappingException
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import kotlin.test.assertEquals


class TestGithub42_FailOnNullForPrimitives {

    data class OptionalIntRequiredBoolean(val optInt: Int = -1, val reqBool: Boolean)

    val mapper = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper().enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

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
