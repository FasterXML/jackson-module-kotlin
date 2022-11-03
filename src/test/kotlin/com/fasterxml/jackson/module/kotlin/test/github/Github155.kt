package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test

class TestGithub155 {
    data class Foo @JvmOverloads constructor(
        @JsonProperty("name") val name: String,
        @JsonProperty("age") val age: Int = 0,
        @JsonProperty("country") val country: String = "whatever",
        @JsonProperty("city") val city: String = "nada")

    @Test
    fun testGithub155() {
        jacksonObjectMapper().readValue<Foo>("""
            {"name":"fred","age":12,"country":"Libertad","city":"Northville"}
        """.trimIndent())

    }
}