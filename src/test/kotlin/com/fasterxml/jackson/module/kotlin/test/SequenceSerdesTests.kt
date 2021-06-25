package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals


class TestSequenceDeserializer {
    data class Data<T>(val value: Sequence<T>)

    @Test
    fun deserializeSequence() {
        val list = listOf("Test", "Test1")
        val objectMapper = jacksonObjectMapper()
        val result = objectMapper.readValue<Data<String>>("{\"value\":[\"Test\",\"Test1\"]}")
        assertEquals(list, result.value.toList())
    }

    @Test
    fun deserializeEmptySequence() {
        val list = listOf<String>()
        val objectMapper = jacksonObjectMapper()
        val result = objectMapper.readValue<Data<String>>("{\"value\":[]}")
        assertEquals(list, result.value.toList())
    }

    @Test
    fun testSerializeSequence() {
        val sequence = listOf("item1", "item2").asSequence()
        val data = Data(sequence)
        val objectMapper = jacksonObjectMapper()
        val result = objectMapper.writeValueAsString(data)
        assertEquals("{\"value\":[\"item1\",\"item2\"]}", result)
    }

    @Test
    fun testSerializeEmptySequence() {
        val sequence = listOf<String>().asSequence()
        val data = Data(sequence)
        val objectMapper =  jacksonObjectMapper()
        val result = objectMapper.writeValueAsString(data)
        assertEquals("{\"value\":[]}", result)
    }

    @Test
    fun testSerializeLazySequence() {
        val sequence = sequence<Map<String, Any>> {
            (0..2).forEach { i ->
                yieldAll((0..2).map { j ->
                    mapOf("key[$i][$j]" to "value[$i][$j]")
                })
            }
        }
        val data = Data(sequence)
        val objectMapper =  jacksonObjectMapper()
        val result = objectMapper.writeValueAsString(data)
        assertEquals(
            """
                {
                    "value":[
                        {"key[0][0]": "value[0][0]"},
                        {"key[0][1]": "value[0][1]"},
                        {"key[0][2]": "value[0][2]"},
                        {"key[1][0]": "value[1][0]"},
                        {"key[1][1]": "value[1][1]"},
                        {"key[1][2]": "value[1][2]"},
                        {"key[2][0]": "value[2][0]"},
                        {"key[2][1]": "value[2][1]"},
                        {"key[2][2]": "value[2][2]"}
                    ]
                }
            """.replace("\\s".toRegex(), ""),
            result
        )
    }
}