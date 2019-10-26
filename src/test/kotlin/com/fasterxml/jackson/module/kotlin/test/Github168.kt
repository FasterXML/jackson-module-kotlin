package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonProperty

import com.fasterxml.jackson.databind.ObjectMapper

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertNotNull

class TestGithub168 {
    class TestClass(@JsonProperty(value = "foo", required = true) foo: String?, val baz: String)

    final val MAPPER : ObjectMapper = jacksonObjectMapper()

    @Test
    fun testIfRequiredIsReallyRequiredWhenNullUsed() {
        val obj = MAPPER.readValue<TestClass>("""{"foo":null,"baz":"whatever"}""")
         assertNotNull(obj)
    }

    @Test(expected = MissingKotlinParameterException::class)
    fun testIfRequiredIsReallyRequiredWhenAbsent() {
        val obj = MAPPER.readValue<TestClass>("""{"baz":"whatever"}""")
        assertNotNull(obj)
    }

    @Test
    fun testIfRequiredIsReallyRequiredWhenValuePresent() {
        val obj = MAPPER.readValue<TestClass>("""{"foo":"yay!","baz":"whatever"}""")
        assertNotNull(obj)
    }
}
