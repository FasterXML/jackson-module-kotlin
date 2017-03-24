package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class Github62 {
    @Test
    fun testAnonymousClassSerialization() {
        val externalValue = "ggg"

        val result = jacksonObjectMapper().writeValueAsString(object {
            val value = externalValue
        })

        assertEquals("""{"value":"ggg"}""", result)
    }
}