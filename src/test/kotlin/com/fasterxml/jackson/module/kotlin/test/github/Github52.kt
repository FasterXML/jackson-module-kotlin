package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.test.assertEquals

class TestGithub52 {
    class Test(
            // Should only be serialized as "is_bar"
            @JsonProperty("is_bar")
            val bar: Boolean = true,

            @JsonProperty("is_bar2")
            val isBar2: Boolean = true,

            // This gets serialized as "is_lol", as expected. No issues here.
            @JsonProperty("is_lol")
            val lol: String = "sdf"
    ) {
        // Should only be serialized as "is_foo"
        @JsonProperty("is_foo")
        val foo: Boolean = true

        @JsonProperty("is_foo2")
        val isFoo2: Boolean = true
    }

    @org.junit.Test
    fun testGithub52() {
        val mapper = jacksonObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)

        val actual = mapper.writeValueAsString(Test())
        val expected = """
            {
              "is_bar":true,
              "is_bar2":true,
              "is_lol":"sdf",
              "is_foo":true,
              "is_foo2":true
            }""".trimIndent()

        assertEquals(expected, actual)
    }
}
