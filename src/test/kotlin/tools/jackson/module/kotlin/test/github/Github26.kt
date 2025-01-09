package tools.jackson.module.kotlin.test.github

import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder
import kotlin.test.assertEquals

data class ClassWithPrimitivesWithDefaults(val i: Int = 5, val x: Int)

class TestGithub26 {
    @Test fun testConstructorWithPrimitiveTypesDefaultedExplicitlyAndImplicitly() {
        val check1: ClassWithPrimitivesWithDefaults = _createMapper()
            .readValue("""{"i":3,"x":2}""")
        assertEquals(3, check1.i)
        assertEquals(2, check1.x)

        val check2: ClassWithPrimitivesWithDefaults = _createMapper()
            .readValue("""{}""")
        assertEquals(5, check2.i)
        assertEquals(0, check2.x)

        val check3: ClassWithPrimitivesWithDefaults = _createMapper()
            .readValue("""{"i": 2}""")
        assertEquals(2, check3.i)
        assertEquals(0, check3.x)

    }

    private fun _createMapper(): ObjectMapper {
        return jacksonMapperBuilder()
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build()
    }

}