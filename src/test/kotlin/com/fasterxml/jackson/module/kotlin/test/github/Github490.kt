package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class Github490 {

    @Test
    fun testKotlinDeserialization() {
        val mapper = jacksonObjectMapper()
        val value: DataClassWithAllNullableParams = mapper.readValue("{" +
                "\"jsonNodeValueWithNullAsDefaultProvidedNull\":null, " +
                "\"jsonNodeValueProvidedNull\":null}")
        assertThat(
            "Nullable missing Int value should be deserialized as null",
            value.intValue,
            CoreMatchers.nullValue()
        )
        assertThat(
            "Nullable missing String value should be deserialized as null",
            value.stringValue,
            CoreMatchers.nullValue()
        )
        assertThat(
            "Nullable missing JsonNode value should be deserialized as null and not as NullNode",
            value.jsonNodeValue,
            CoreMatchers.nullValue()
        )
        assertThat(
            "Nullable missing JsonNode value should be deserialized as null and not as NullNode",
            value.jsonNodeValueProvidedNull,
            CoreMatchers.nullValue()
        )
        assertThat(
            "Nullable by default missing JsonNode value should be deserialized as null and not as NullNode",
            value.jsonNodeValueWithNullAsDefault,
            CoreMatchers.nullValue()
        )
        assertThat(
            "Nullable by default JsonNode with provided null value in payload should be deserialized as null and not as NullNode",
            value.jsonNodeValueWithNullAsDefaultProvidedNull,
            CoreMatchers.nullValue()
        )
    }
}

data class DataClassWithAllNullableParams(
    val intValue: Int?,
    val stringValue: String?,
    val jsonNodeValue: JsonNode?,
    val jsonNodeValueProvidedNull: JsonNode?,
    val jsonNodeValueWithNullAsDefault: JsonNode? = null,
    val jsonNodeValueWithNullAsDefaultProvidedNull: JsonNode? = null
)