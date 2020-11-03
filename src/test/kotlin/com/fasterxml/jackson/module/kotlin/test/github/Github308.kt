package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class Github308 {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TestDto(
            @JsonIgnore
            var id: Long? = null,
            var cityId: Int? = null
    ) {
        @JsonProperty("id")
        private fun unpackId(idObj: Int?) {
            cityId = idObj
        }
    }

    @Test
    fun createTestDto() {
        val dto: TestDto = jacksonObjectMapper().readValue("""{"id":12345}""")

        assertNotNull(dto)
        assertNull(dto.id)
        assertEquals(dto.cityId, 12345)
    }
}