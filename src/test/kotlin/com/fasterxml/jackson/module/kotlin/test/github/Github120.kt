package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import junit.framework.Assert.assertEquals
import org.junit.Test

class TestGithub120 {
    data class Foo @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor (
            @JsonValue
            val value: Long
    )

    data class Bar(
            val foo: Foo
    )

    @Test
    fun testNestedJsonValue() {
        val om = jacksonObjectMapper()
        val foo = Foo(4711L)
        val bar = Bar(foo)
        val asString = om.writeValueAsString(bar)
        assertEquals("{\"foo\":4711}", asString)

        val fromString = om.readValue(asString, Bar::class.java)
        assertEquals(bar, fromString)
    }
}