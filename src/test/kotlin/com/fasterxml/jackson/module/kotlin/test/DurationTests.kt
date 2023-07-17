package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class DurationTests {
    @Test
    fun `should serialize Kotlin duration using Java time module`() {
        val mapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(WRITE_DURATIONS_AS_TIMESTAMPS)

        val result = mapper.writeValueAsString(1.hours)

        assertEquals("\"PT1H\"", result)
    }

    @Test
    fun `should deserialize Kotlin duration`() {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())

        val result = mapper.readValue<Duration>("\"PT1H\"")

        assertEquals(1.hours, result)
    }

    @Test
    fun `should serialize Kotlin duration inside list using Java time module`() {
        val mapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(WRITE_DURATIONS_AS_TIMESTAMPS)

        val result = mapper.writeValueAsString(listOf(1.hours, 2.hours, 3.hours))

        assertEquals("""["PT1H","PT2H","PT3H"]""", result)
    }

    @Test
    fun `should deserialize Kotlin duration inside list`() {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())

        val result = mapper.readValue<List<Duration>>("""["PT1H","PT2H","PT3H"]""")

        assertContentEquals(listOf(1.hours, 2.hours, 3.hours), result)
    }

    @Test
    fun `should serialize Kotlin duration inside map using Java time module`() {
        val mapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(WRITE_DURATIONS_AS_TIMESTAMPS)

        val result = mapper.writeValueAsString(mapOf(
            "a" to 1.hours,
            "b" to 2.hours,
            "c" to 3.hours
        ))

        assertEquals("""{"a":"PT1H","b":"PT2H","c":"PT3H"}""", result)
    }

    @Test
    fun `should deserialize Kotlin duration inside map`() {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())

        val result = mapper.readValue<Map<String, Duration>>("""{"a":"PT1H","b":"PT2H","c":"PT3H"}""")

        assertEquals(result["a"], 1.hours)
        assertEquals(result["b"], 2.hours)
        assertEquals(result["c"], 3.hours)
    }
}