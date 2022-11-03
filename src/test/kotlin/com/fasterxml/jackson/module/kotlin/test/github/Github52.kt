package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.test.assertEquals

class TestGithub52 {
    private val mapper = jacksonObjectMapper()

    @org.junit.Test
    fun testBooleanPropertyInConstructor() {
        data class BooleanPropertyInConstructor(
                @JsonProperty("is_bar")
                val bar: Boolean = true
        )

        assertEquals("""{"is_bar":true}""", mapper.writeValueAsString(BooleanPropertyInConstructor()))
    }

    @org.junit.Test
    fun testIsPrefixedBooleanPropertyInConstructor() {
        data class IsPrefixedBooleanPropertyInConstructor(
                @JsonProperty("is_bar2")
                val isBar2: Boolean = true
        )

        assertEquals("""{"is_bar2":true}""", mapper.writeValueAsString(IsPrefixedBooleanPropertyInConstructor()))
    }

    @org.junit.Test
    fun testIsPrefixedStringPropertyInConstructor() {
        data class IsPrefixedStringPropertyInConstructor(
                @JsonProperty("is_lol")
                val lol: String = "sdf"
        )

        assertEquals("""{"is_lol":"sdf"}""", mapper.writeValueAsString(IsPrefixedStringPropertyInConstructor()))
    }

    @org.junit.Test
    fun testBooleanPropertyInBody() {
        data class BooleanPropertyInBody(
                @JsonIgnore val placeholder: String = "placeholder"
        ) {
            @JsonProperty("is_foo")
            val foo: Boolean = true
        }

        assertEquals("""{"is_foo":true}""", mapper.writeValueAsString(BooleanPropertyInBody()))
    }

    @org.junit.Test
    fun testIsPrefixedBooleanPropertyInBody() {
        data class IsPrefixedBooleanPropertyInBody(
                @JsonIgnore val placeholder: String = "placeholder"
        ) {
            @JsonProperty("is_foo2")
            val isFoo2: Boolean = true
        }

        assertEquals("""{"is_foo2":true}""", mapper.writeValueAsString(IsPrefixedBooleanPropertyInBody()))
    }
}
