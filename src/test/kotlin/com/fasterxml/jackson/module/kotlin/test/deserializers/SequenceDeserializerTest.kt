package com.fasterxml.jackson.module.kotlin.test.deserializers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.deserializers.SequenceDeserializer
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Konstantin Volivach
 */
class SequenceDeserializerTest {
    data class Data(
        @field:JsonDeserialize(using = SequenceDeserializer::class)
        val value: Sequence<String>
    )

    @Test
    fun deserializeSequence() {
        val list = listOf("Test", "Test1")
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(KotlinModule())
        val result = objectMapper.readValue<Data>("{\"value\":[\"Test\",\"Test1\"]}")
        assertEquals(list, result.value.toList())
    }

    @Test
    fun deserializeEmptySequence() {
        val list = listOf<String>()
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(KotlinModule())
        val result = objectMapper.readValue<Data>("{\"value\":[]}")
        assertEquals(list, result.value.toList())
    }
}