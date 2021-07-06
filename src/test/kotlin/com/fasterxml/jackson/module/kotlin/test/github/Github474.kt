package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub474 {
    @Test
    fun jsonPropertyAnnotationRespectedOnParentClass() {
        open class Parent(@JsonProperty("parent-prop") val parent: String)
        class Child(@JsonProperty("child-prop") val child: String) : Parent(child)

        assertEquals(
            """{"child-prop":"foo","parent-prop":"foo"}""",
            jacksonObjectMapper().writeValueAsString(Child("foo"))
        )
    }
}
