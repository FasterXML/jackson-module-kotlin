package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Ignore
import org.junit.Test


class TestGithub50 {
    data class Name(val firstName: String, val lastName: String)

    data class Employee(
        @get:JsonUnwrapped val name: Name,
        val position: String
    )


    @Test
    @Ignore("""Fails with: Cannot define Creator property "name" as `@JsonUnwrapped`""")
    fun testGithub50UnwrappedError() {
        val JSON = """{"firstName":"John","lastName":"Smith","position":"Manager"}"""
        val obj: Employee = jacksonObjectMapper().readValue(JSON)
    }
}