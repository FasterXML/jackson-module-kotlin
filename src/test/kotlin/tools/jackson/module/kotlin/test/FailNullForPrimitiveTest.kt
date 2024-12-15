package tools.jackson.module.kotlin.test

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.exc.MismatchedInputException
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class FailNullForPrimitiveTest {
    val mapper = jacksonMapperBuilder()
        .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        .build()

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
