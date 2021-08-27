package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

// Failed with MissingKotlinParameterException in 2.13.0-rc1
class TestGithub484 {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DataHolder(val data: String)

    @Test
    fun testIgnoreUnknownDeserializationArray() {
        val holder = jacksonObjectMapper().readValue<DataHolder>("""{ "ignored": [], "data": "string" }""")
        assertEquals("string", holder.data)
    }

    @Test
    fun testIgnoreUnknownDeserializationObject() {
        val holder = jacksonObjectMapper().readValue<DataHolder>("""{ "ignored": {}, "data": "string" }""")
        assertEquals("string", holder.data)
    }
}
