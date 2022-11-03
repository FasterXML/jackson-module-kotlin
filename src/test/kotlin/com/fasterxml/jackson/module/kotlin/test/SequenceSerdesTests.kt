package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals


class TestSequenceDeserializer {
    data class Data(val value: Sequence<String>)

    @Test
    fun deserializeSequence() {
        val list = listOf("Test", "Test1")
        val objectMapper = jacksonObjectMapper()
        val result = objectMapper.readValue<Data>("{\"value\":[\"Test\",\"Test1\"]}")
        assertEquals(list, result.value.toList())
    }

    @Test
    fun deserializeEmptySequence() {
        val list = listOf<String>()
        val objectMapper = jacksonObjectMapper()
        val result = objectMapper.readValue<Data>("{\"value\":[]}")
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
}