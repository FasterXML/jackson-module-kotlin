package tools.jackson.module.kotlin.test.github

import tools.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub88 {
    class CloneableKotlinObj(val id: String) : Cloneable

    @Test
    fun shouldDeserializeSuccessfullyKotlinCloneableObject() {
        val result = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper().writeValueAsString(CloneableKotlinObj("123"))

        assertEquals("{\"id\":\"123\"}", result)
    }

    @Test
    fun shouldDeserializeSuccessfullyJavaCloneableObject() {
        val result = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper()
            .writeValueAsString(tools.jackson.module.kotlin.test.github.CloneableJavaObj("123"))

        assertEquals("{\"id\":\"123\"}", result)
    }
}
