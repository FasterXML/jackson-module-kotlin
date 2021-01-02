package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestGithub271 {
    @JsonPropertyOrder(alphabetic=true)
    data class Foo(val a: String, val c: String) {
        val b = "b"
    }

    @Test
    fun testAlphabeticFields() {
        val mapper = jacksonObjectMapper()

        val json = mapper.writeValueAsString(Foo("a", "c"))
        try {
            assertEquals("""{"a":"a","b":"b","c":"c"}""", json)
            fail("GitHub #271 has been fixed!")
        } catch (e: AssertionError) {
            // Remove this try/catch and the `fail()` call above when this issue is fixed
        }
    }
}
