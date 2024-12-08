package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class TestGithub168 {
    @Suppress("UNUSED_PARAMETER")
    class TestClass(@JsonProperty(value = "foo", required = true) foo: String?, val baz: String)

    @Test
    fun testIfRequiredIsReallyRequiredWhenNullused() {
        val obj = jacksonObjectMapper().readValue<TestClass>("""{"foo":null,"baz":"whatever"}""")
        assertEquals("whatever", obj.baz)
    }

    @Test
    fun testIfRequiredIsReallyRequiredWhenAbsent() {
        assertThrows<MissingKotlinParameterException> {
            val obj = jacksonObjectMapper().readValue<TestClass>("""{"baz":"whatever"}""")
            assertEquals("whatever", obj.baz)
        }
    }

    @Test
    fun testIfRequiredIsReallyRequiredWhenVauePresent() {
        val obj = jacksonObjectMapper().readValue<TestClass>("""{"foo":"yay!","baz":"whatever"}""")
        assertEquals("whatever", obj.baz)
    }
}
