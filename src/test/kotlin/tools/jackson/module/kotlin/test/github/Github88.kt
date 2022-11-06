package tools.jackson.module.kotlin.test.github

import tools.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub88 {
    class CloneableKotlinObj(val id: String) : Cloneable

    @Test
    fun shouldDeserializeSuccessfullyKotlinCloneableObject() {
        val result = jacksonObjectMapper().writeValueAsString(CloneableKotlinObj("123"))

        assertEquals("{\"id\":\"123\"}", result)
    }

    @Test
    fun shouldDeserializeSuccessfullyJavaCloneableObject() {
        val result = jacksonObjectMapper()
            .writeValueAsString(tools.jackson.module.kotlin.test.github.CloneableJavaObj("123"))

        assertEquals("{\"id\":\"123\"}", result)
    }
}
