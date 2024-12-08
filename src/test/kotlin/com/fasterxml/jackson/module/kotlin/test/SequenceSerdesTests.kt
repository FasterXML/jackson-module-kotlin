package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
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

    class ContentSer : StdSerializer<String>(String::class.java) {
        override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
            provider.defaultSerializeValue("$value-ser", gen)
        }
    }

    data class ListWrapper(
        @JsonSerialize(contentUsing = ContentSer::class) val value: List<String>
    )

    data class SequenceWrapper(
        @JsonSerialize(contentUsing = ContentSer::class)
        val value: Sequence<String>
    )

    @Test
    fun contentUsingTest() {
        val mapper = jacksonObjectMapper()

        val listResult = mapper.writeValueAsString(ListWrapper(listOf("foo")))
        val sequenceResult = mapper.writeValueAsString(SequenceWrapper(sequenceOf("foo")))

        assertEquals("""{"value":["foo-ser"]}""", sequenceResult)
        assertEquals(listResult, sequenceResult)
    }

    // @see #674
    @Test
    fun sequenceOfTest() {
        val mapper = jacksonObjectMapper()
        val result = mapper.writeValueAsString(sequenceOf("foo"))

        assertEquals("""["foo"]""", result)
    }
}
