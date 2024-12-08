package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

// Also see https://github.com/FasterXML/jackson-databind/issues/3392
class Github242 {
    data class IntValue(val value: Int)

    private val objectMapper = jacksonObjectMapper()

    // Since `nullish` is entered for a `non-null` value, deserialization is expected to fail,
    // but at the moment the process continues with the default value set on the `databind`.
    @Test
    fun `test value throws - Int`(){
        val v0 =objectMapper.readValue<IntValue>("{}")
        val v1 =objectMapper.readValue<IntValue>("{\"value\":null}")

        assertEquals(0, v0.value)
        assertEquals(v0, v1)
    }
}
