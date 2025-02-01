package com.fasterxml.jackson.module.kotlin.kogeraIntegration.ser.valueClass.jsonInclude

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonIncludeCustomTest {
    class NullFilter {
        override fun equals(other: Any?) = other == null
    }

    @JsonInclude(
        value = JsonInclude.Include.CUSTOM,
        valueFilter = NullFilter::class
    )
    data class NullFilterDto(
        val pN: Primitive? = null,
        val nnoN: NonNullObject? = null,
        val noN1: NullableObject? = null
    )

    @Test
    fun nullFilterTest() {
        val mapper = jacksonObjectMapper()
        val dto = NullFilterDto()
        assertEquals("{}", mapper.writeValueAsString(dto))
    }
}
