package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TestGithub490 {
    val mapper = jacksonObjectMapper()
    val value: DataClassWithAllNullableParams = mapper.readValue(
        "{" +
                "\"jsonNodeValueWithNullAsDefaultProvidedNull\":null, " +
                "\"jsonNodeValueProvidedNull\":null}"
    )

    @Test
    fun testKotlinDeserialization_intValue() {
        assertThat(
            "Nullable missing Int value should be deserialized as null",
            value.intValue,
            CoreMatchers.nullValue()
        )
    }

    @Test
    fun testKotlinDeserialization_stringValue() {
        assertThat(
            "Nullable missing String value should be deserialized as null",
            value.stringValue,
            CoreMatchers.nullValue()
        )
    }

    @Test
    fun testKotlinDeserialization_jsonNodeValue() {
        assertThat(
            "Nullable missing JsonNode value should be deserialized as null and not as NullNode",
            value.jsonNodeValue,
            CoreMatchers.nullValue()
        )
    }

    @Test
    fun testKotlinDeserialization_jsonNodeValueProvidedNull() {
        @Suppress("UNCHECKED_CAST") // For Kotlin 1.3 version of test
        assertThat(
            "Nullable JsonNode value provided as null should be deserialized as NullNode",
            value.jsonNodeValueProvidedNull,
            CoreMatchers.equalTo(NullNode.instance) as Matcher<in JsonNode?>
        )
    }

    @Test
    fun testKotlinDeserialization_jsonNodeValueWithNullAsDefault() {
        assertThat(
            "Nullable by default missing JsonNode value should be deserialized as null and not as NullNode",
            value.jsonNodeValueWithNullAsDefault,
            CoreMatchers.nullValue()
        )
    }

    @Test
    fun testKotlinDeserialization_jsonNodeValueWithNullAsDefaultProvidedNull() {
        @Suppress("UNCHECKED_CAST") // For Kotlin 1.3 version of test
        assertThat(
            "Nullable by default JsonNode with provided null value in payload should be deserialized as NullNode",
            value.jsonNodeValueWithNullAsDefaultProvidedNull,
            CoreMatchers.equalTo(NullNode.instance) as Matcher<in JsonNode?>
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
