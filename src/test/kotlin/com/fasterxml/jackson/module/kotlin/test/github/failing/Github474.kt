package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.ComparisonFailure
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestGithub474 {
    @Test
    fun jsonPropertyAnnotationRespectedOnParentClass() {
        open class Parent(@JsonProperty("parent-prop") val parent: String)
        class Child(@JsonProperty("child-prop") val child: String) : Parent(child)

        expectFailure<ComparisonFailure>("GitHub #474 has been fixed!") {
            assertEquals(
                """{"child-prop":"foo","parent-prop":"foo"}""",
                jacksonObjectMapper().writeValueAsString(Child("foo"))
            )
        }
    }
}
