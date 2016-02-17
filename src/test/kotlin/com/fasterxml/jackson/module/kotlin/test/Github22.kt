package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.module.kotlin.*
import org.junit.Test
import java.io.File
import java.io.PrintWriter
import kotlin.test.assertEquals

class StringValue constructor (s: String) {
    val other: String = s

    @JsonValue override fun toString() = other
}

data class StringValue2(@get:JsonIgnore val s: String) {
    @JsonValue override fun toString() = s
}

class TestGithub22 {
    @Test fun testJsonValueNoMatchingMemberWithConstructor() {
        val expectedJson = "\"test\""
        val expectedObj = StringValue("test")

        val actualJson = jacksonObjectMapper().writeValueAsString(expectedObj)
        assertEquals(expectedJson, actualJson)

        val actualObj = jacksonObjectMapper().readValue<StringValue>("\"test\"")
        assertEquals(expectedObj.other, actualObj.other)

    }

    @Test fun testJsonValue2DataClassIgnoredMemberInConstructor() {
        val expectedJson = "\"test\""
        val expectedObj = StringValue2("test")

        val actualJson = jacksonObjectMapper().writeValueAsString(expectedObj)
        assertEquals(expectedJson, actualJson)

        val actualObj = jacksonObjectMapper().readValue<StringValue2>("\"test\"")
        assertEquals(expectedObj, actualObj)

    }
}