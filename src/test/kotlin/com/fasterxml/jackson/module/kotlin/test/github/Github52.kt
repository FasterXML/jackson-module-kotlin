package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.test.assertEquals

class TestGithub52 {
    class Test(
        // This gets serialized as "is_lol", as expected. No issues here.
        @JsonProperty("is_lol")
        val lol: String = "sdf",

        // This gets serialized as "bar", even though we asked for "is_bar". This is an issue.
        @JsonProperty("is_bar")
        val bar: Boolean = true,

        @JsonProperty("is_bar2")
        val isBar2: Boolean = true) {

        // This gets serialized as both "foo" and "is_foo", although we only asked for "is_foo". Also an issue.
        @JsonProperty("is_foo")
        val foo: Boolean = true

        @JsonProperty("is_foo2")
        val isFoo2: Boolean = true
    }

    @org.junit.Test
    fun testGithub52() {
        val mapper = jacksonObjectMapper()

        val actual = mapper.writeValueAsString(Test())
        val expected = """{"is_lol":"sdf","is_bar":true,"is_bar2":true,"is_foo":true,"is_foo2":true}"""

        // error
        // {"is_lol":"sdf",
        //  "is_bar":true,
        //  "bar2":true,  ... should be is_bar2
        //  "foo2":true,  ... should not be here
        //  "is_foo":true,
        //  "is_foo2":true}
        assertEquals(expected, actual)
    }
}
