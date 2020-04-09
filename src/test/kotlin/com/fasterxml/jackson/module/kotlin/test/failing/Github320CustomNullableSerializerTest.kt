package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class Github320CustomNullableSerializerTest {

    val objectMapper = jacksonMapperBuilder()
            .addModule(SimpleModule()
                    .addSerializer(Double::class.java, RoundingSerializer())
                    .addDeserializer(Double::class.java, RoundingDeserializer()))
            .build()

    @Test
    fun `test custom serializing passing object with null and not nullable object types expecting both values rounded`() {
        val jsonString = objectMapper.writeValueAsString(TestDoubleData(nonNullVal = 1.5567, nullVal = 1.5567))
        val testResult = objectMapper.readValue(jsonString, TestDoubleData::class.java)
        assertThat(testResult.nonNullVal, equalTo(1.56))
        assertThat(testResult.nullVal, equalTo(1.56))
    }

    @Test
    fun `test custom deserializer passing json string expecting object deserialized with rounding deserializer applied`() {
        val testResult = objectMapper.readValue<TestDoubleData>("""
            {
                "nonNullVal":1.5567,
                "nullVal":1.5567
            }
        """.trimIndent())
        assertThat(testResult.nonNullVal, equalTo(1.56))
        assertThat(testResult.nullVal, equalTo(1.56))
    }
}

data class TestDoubleData(
        val nonNullVal: Double,
        val nullVal: Double?
)

class RoundingSerializer : JsonSerializer<Double?>() {
    override fun serialize(value: Double?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.let {
            gen?.writeNumber(BigDecimal(it).setScale(2, RoundingMode.HALF_UP))
        }
    }
}

class RoundingDeserializer : JsonDeserializer<Double?>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Double? {
        return BigDecimal(p?.valueAsString)
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
    }

}