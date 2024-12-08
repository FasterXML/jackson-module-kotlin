package tools.jackson.module.kotlin.test.github

import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.NullNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TestGithub490 {
    val mapper = jacksonObjectMapper()
    val value: DataClassWithAllNullableParams = mapper.readValue(
        "{" +
                "\"jsonNodeValueWithNullAsDefaultProvidedNull\":null, " +
                "\"jsonNodeValueProvidedNull\":null}"
    )

    @Test
    fun testKotlinDeserialization_intValue() {
        assertNull(value.intValue, "Nullable missing Int value should be deserialized as null")
    }

    @Test
    fun testKotlinDeserialization_stringValue() {
        assertNull(value.stringValue, "Nullable missing String value should be deserialized as null")
    }

    @Test
    fun testKotlinDeserialization_jsonNodeValue() {
        assertNull(
            value.jsonNodeValue,
            "Nullable missing JsonNode value should be deserialized as null and not as NullNode"
        )
    }

    @Test
    fun testKotlinDeserialization_jsonNodeValueProvidedNull() {
        assertEquals(
            NullNode.instance,
            value.jsonNodeValueProvidedNull,
            "Nullable JsonNode value provided as null should be deserialized as NullNode"
        )
    }

    @Test
    fun testKotlinDeserialization_jsonNodeValueWithNullAsDefault() {
        assertNull(
            value.jsonNodeValueWithNullAsDefault,
            "Nullable by default missing JsonNode value should be deserialized as null and not as NullNode"
        )
    }

    @Test
    fun testKotlinDeserialization_jsonNodeValueWithNullAsDefaultProvidedNull() {
        assertEquals(
            NullNode.instance,
            value.jsonNodeValueWithNullAsDefaultProvidedNull,
            "Nullable by default JsonNode with provided null value in payload should be deserialized as NullNode"
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
