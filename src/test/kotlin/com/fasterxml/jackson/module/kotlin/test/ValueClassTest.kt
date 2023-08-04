package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Test

class ValueClassTest {
    @JvmInline
    value class UserId(val rawValue: String)

    data class User(
        val id: UserId,
        val name: String,
    )

    data class Customer(
        @JsonProperty("customerId")
        val id: UserId,
        @JsonProperty("customerName")
        val name: String,
    )

    @Test
    fun `deserialize value class correctly`() {
        val json: String = """
            "1111"
        """.trimIndent()

        val deserialized: UserId = createMapper().readValue(json)

        Assert.assertEquals("1111", deserialized.rawValue)
    }

    @Test
    fun `deserialize complex object with value class`() {
        val json: String = """
            {
                "id": "1111",
                "name": "foo"
            }
        """.trimIndent()

        val deserialized: User = createMapper().readValue(json)

        Assert.assertEquals("1111", deserialized.id.rawValue)
    }

    @Test
    fun `deserialize complex annotated object with value class`() {
        val json: String = """
            {
                "customerId": "1111",
                "customerName": "foo"
            }
        """.trimIndent()

        val deserialized: Customer = createMapper().readValue(json)

        Assert.assertEquals("1111", deserialized.id.rawValue)
    }

    private fun createMapper(): ObjectMapper {
        return ObjectMapper().registerModule(kotlinModule())
    }
}
