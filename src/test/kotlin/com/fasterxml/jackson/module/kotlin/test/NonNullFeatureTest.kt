package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class NonNullFeatureTest {
    val objectMapper = jacksonObjectMapper()
        .registerModule(SimpleModule())
//        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    @JvmInline
    value class Wrapped(val rawValue: String)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class NonNullDto (
        val text: String? = null,
        val wrapped: Wrapped? = null,
    )

    data class NullDto (
        val text: String? = null,
        val wrapped: Wrapped? = null,
    )

    @Test
    fun `should not serialize null values with NonNull`() {
        val nonNullDto = NonNullDto()
        val expected = "{}"

        val result = objectMapper.writeValueAsString(nonNullDto)

        assertEquals(result, expected)
    }

    @Test
    fun `should serialize null values with Null`() {
        val nonNullDto = NullDto()
        val expected = "{\"text\":null,\"wrapped\":null}"

        val result = objectMapper.writeValueAsString(nonNullDto)

        assertEquals(result, expected)
    }
}