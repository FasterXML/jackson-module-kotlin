package tools.jackson.module.kotlin.test

import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertThrows
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.exc.MismatchedInputException
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import kotlin.test.Test

class FailNullForPrimitiveTest {
    data class Dto(
        val foo: Int,
        val bar: Int?
    )

    @Test
    fun test() {
        val mapper = jacksonMapperBuilder()
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
            .build()

        assertThrows(MismatchedInputException::class.java) {
            mapper.readValue<Dto>("{}")
        }

        assertThrows(MismatchedInputException::class.java) {
            mapper.readValue<Dto>("""{"foo":null}""")
        }

        assertEquals(Dto(0, null), mapper.readValue<Dto>("""{"foo":0}"""))
    }
}
