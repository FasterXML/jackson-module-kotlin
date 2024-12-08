package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestGithub207 {
    open class Wrapper(@JsonValue val value: String) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Wrapper

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int =
                value.hashCode()

        override fun toString(): String =
                value

    }

    class ExtendedWrapper(value: String) : Wrapper(value)

    private val objectMapper = ObjectMapper()

    @Test
    fun shouldDeserializeJsonString() {
        // given
        val json = "\"foo\""

        // when
        val deserialized = objectMapper.readValue<Wrapper>(json)

        // then
        assertEquals(Wrapper("foo"), deserialized)
    }

    @Test
    fun shouldSerializeJsonString() {
        // given
        val wrapperObject = Wrapper("foo")

        // when
        val serialized = objectMapper.writeValueAsString(wrapperObject)

        // then
        assertEquals("\"foo\"", serialized)
    }

    @Test
    fun shouldDeserializeJsonString_Extended() {
        // given
        val json = "\"foo\""

        // when
        val deserialized = objectMapper.readValue<ExtendedWrapper>(json)

        // then
        assertEquals(ExtendedWrapper("foo"), deserialized)
    }

    @Test
    fun shouldSerializeJsonString_Extended() {
        // given
        val wrapperObject = ExtendedWrapper("foo")

        // when
        val serialized = objectMapper.writeValueAsString(wrapperObject)

        // then
        assertEquals("\"foo\"", serialized)
    }

}