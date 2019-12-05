package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub269 {
    data class Foo(val pattern: Regex)
    data class Bar(val thing: Regex)

    @Test
    fun testGithub269WithFoo() {
        val mapper = jacksonObjectMapper()

        val testObject = Foo(Regex("test"))
        val testJson = mapper.writeValueAsString(testObject)
        val resultObject = mapper.readValue<Foo>(testJson)

        assertEquals(testObject.pattern.pattern, resultObject.pattern.pattern)
        assertEquals(testObject.pattern.options, resultObject.pattern.options)

        mapper.readValue<Foo>("""{"pattern":"test"}""")
    }

    @Test
    fun testGithub269WithBar() {
        val mapper = jacksonObjectMapper()

        val testObject = Bar(Regex("test"))
        val testJson = mapper.writeValueAsString(testObject)
        val resultObject = mapper.readValue<Bar>(testJson)

        assertEquals(testObject.thing.pattern, resultObject.thing.pattern)
        assertEquals(testObject.thing.options, resultObject.thing.options)

        mapper.readValue<Bar>("""{"thing":"test"}""")
    }
}