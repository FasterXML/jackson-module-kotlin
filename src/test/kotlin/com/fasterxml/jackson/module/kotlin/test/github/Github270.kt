package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestGithub270 {
    data class Wrapper(private val field: String) {
        val upper = field.uppercase()
        fun field() = field
        fun stillAField() = field
    }

    @Test
    fun testPublicFieldOverlappingFunction() {
        val json = jacksonObjectMapper().writeValueAsString(Wrapper("Hello"))
        assertEquals("""{"upper":"HELLO"}""", json)
    }
}
