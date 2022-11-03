package tools.jackson.module.kotlin.test.github

import tools.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub62 {
    @Test
    fun testAnonymousClassSerialization() {
        val externalValue = "ggg"

        val result = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper().writeValueAsString(object {
            val value = externalValue
        })

        assertEquals("""{"value":"ggg"}""", result)
    }
}