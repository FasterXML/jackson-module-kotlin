package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Test

class ValueClassTest {
    @JvmInline
    value class UserId(val rawValue: String)

    @Test
    fun `deserialize value class correctly`() {
        val json: String = """
            "1111"
        """.trimIndent()

        val deserialized: UserId = createMapper().readValue(json)

        Assert.assertEquals("1111", deserialized.rawValue)
    }

    private fun createMapper(): ObjectMapper {
        return ObjectMapper().registerModule(kotlinModule())
    }
}
