package com.fasterxml.jackson.module.kotlin.test.serializers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.serializers.SequenceSerializer
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Konstantin Volivach
 */
class SequenceSerializerTest {

    data class Data(
        @field:JsonSerialize(using = SequenceSerializer::class)
        val value: Sequence<String>
    )

    @Test
    fun testSerializeSequence() {
        val sequence = listOf("Test", "Test1").asSequence()
        val data = Data(
            sequence
        )
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(KotlinModule())
        val result = objectMapper.writeValueAsString(data)
        assertEquals("{\"value\":[\"Test\",\"Test1\"]}", result)
    }

    @Test
    fun testSerializeEmptySequence() {
        val sequence = listOf<String>().asSequence()
        val data = Data(
            sequence
        )
        val objectMapper = ObjectMapper()
        val result = objectMapper.writeValueAsString(data)
        assertEquals("{\"value\":[]}", result)
    }
}