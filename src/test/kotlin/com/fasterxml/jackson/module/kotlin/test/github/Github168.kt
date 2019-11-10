package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test

class TestGithub168 {
    class TestClass(@JsonProperty(value = "foo", required = true) foo: String?, val baz: String)

    @Test
    fun testIfRequiredIsReallyRequiredWhenNullused() {
        val obj = jacksonObjectMapper().readValue<TestClass>("""{"foo":null,"baz":"whatever"}""")
    }

    @Test(expected = MissingKotlinParameterException::class)
    fun testIfRequiredIsReallyRequiredWhenAbsent() {
        val obj = jacksonObjectMapper().readValue<TestClass>("""{"baz":"whatever"}""")
    }

    @Test
    fun testIfRequiredIsReallyRequiredWhenVauePresent() {
        val obj = jacksonObjectMapper().readValue<TestClass>("""{"foo":"yay!","baz":"whatever"}""")
    }
}