package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.ComparisonFailure
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Custom serializers for generic values don't work
 */
class TestGithub431 {
    private val mapper = jacksonObjectMapper()
        .registerModule(kotlinModule())
        .registerModule(SimpleModule().addSerializer(Long::class.java, LongSerializer))
        .registerModule(SimpleModule().addSerializer(String::class.java, StringSerializer))

    @Test
    fun serializeLongList() {
        expectFailure<ComparisonFailure>("Github 431 has been fixed!") {
            assertEquals("""["1232"]""", mapper.writeValueAsString(listOf(1232L)))
        }
    }

    @Test
    fun serializeLongMapValue() {
        expectFailure<ComparisonFailure>("Github 431 has been fixed!") {
            assertEquals("""{"longValue":"123"}""", mapper.writeValueAsString(mapOf("longValue" to 123L)))
        }
    }

    @Test
    fun serializeGenericDataClass() {
        class GenericDataClass<T>(var data: T?) {
            val time: Long = 123L
        }

        expectFailure<ComparisonFailure>("Github 431 has been fixed!") {
            assertEquals("""{"data":"12","time":"123"}""", mapper.writeValueAsString(GenericDataClass(12L)))
        }
    }

    // Works fine for non-generic classes
    @Test
    fun serializeLongDataClass() {
        class LongDataClass(val data: Long)

        assertEquals("""{"data":"12"}""", mapper.writeValueAsString(LongDataClass(12L)))
    }

    @Test
    fun serializetStringList() {
        assertEquals("""["foo-bar"]""", mapper.writeValueAsString(listOf("bar")))
    }
}

object LongSerializer : JsonSerializer<Long>() {
    override fun serialize(value: Long, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeString("$value")
    }
}

object StringSerializer : JsonSerializer<String>() {
    override fun serialize(value: String, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeString("foo-$value")
    }
}

