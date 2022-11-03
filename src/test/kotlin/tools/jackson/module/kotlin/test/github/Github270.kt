package tools.jackson.module.kotlin.test.github

import tools.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub270 {
    data class Wrapper(private val field: String) {
        val upper = field.uppercase()
        fun field() = field
        fun stillAField() = field
    }

    @Test
    fun testPublicFieldOverlappingFunction() {
        val json = jacksonObjectMapper().writeValueAsString(Wrapper("Hello"))
        assertEquals("""{"upper":"HELLO"}""", json)
    }
}
