package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub80 {

    @Test
    fun testIsBool() {
        val mapper = jacksonObjectMapper()

        val example = IsBoolExample(true)
        val json = mapper.writeValueAsString(example)
        assertEquals("{\"isTrueOrFalse\":true}", json)

        val deserialized = mapper.readValue(json, IsBoolExample::class.java)
        assertEquals(example.isTrueOrFalse, deserialized.isTrueOrFalse)
    }

    @Test
    fun testAnnotatedIsBool() {
        val mapper = jacksonObjectMapper()

        val example = IsBoolAnnotatedExample(true)
        val json = mapper.writeValueAsString(example)
        assertEquals("{\"isTrueOrFalse\":true}", json)

        val deserialized = mapper.readValue(json, IsBoolAnnotatedExample::class.java)
        assertEquals(example.isTrue, deserialized.isTrue)
    }

    class IsBoolExample(val isTrueOrFalse: Boolean)

    class IsBoolAnnotatedExample(@JsonProperty("isTrueOrFalse") val isTrue: Boolean)
}