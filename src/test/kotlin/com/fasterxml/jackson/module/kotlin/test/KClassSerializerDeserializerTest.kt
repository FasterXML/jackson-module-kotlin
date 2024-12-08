package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.addDeserializer
import com.fasterxml.jackson.module.kotlin.addSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode

class KClassSerializerDeserializerTest {

    val objectMapper = jacksonObjectMapper()
            .registerModule(SimpleModule()
                    .addSerializer(Double::class, RoundingSerializer())
                    .addDeserializer(Double::class, RoundingDeserializer()))


    @Test
    fun `test custom serializer expecting object serialized with rounding serializer applied`() {
        val jsonString = objectMapper.writeValueAsString(TestDoubleData(nonNullVal = 1.5567, nullVal = 1.5678))
        val testResult = objectMapper.readValue(jsonString, TestDoubleData::class.java)
        assertEquals(1.56, testResult.nonNullVal)
        assertEquals(1.57, testResult.nullVal)
    }

    @Test
    fun `test custom deserializer expecting object deserialized with rounding deserializer applied`() {
        val testResult = objectMapper.readValue<TestDoubleData>("""
            {
                "nonNullVal":1.5567,
                "nullVal":1.5678
            }
        """.trimIndent())
        assertEquals(1.56, testResult.nonNullVal)
        assertEquals(1.57, testResult.nullVal)
    }
}

data class TestDoubleData(
        val nonNullVal: Double,
        val nullVal: Double?
)

class RoundingSerializer : JsonSerializer<Double>() {
    override fun serialize(value: Double?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.let {
            gen?.writeNumber(BigDecimal(it).setScale(2, RoundingMode.HALF_UP))
        }
    }
}

class RoundingDeserializer : JsonDeserializer<Double>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Double? {
        return BigDecimal(p?.valueAsString)
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
    }
}
