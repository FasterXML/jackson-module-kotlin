package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.Test

class TestGithub50 {
    data class Name(val firstName: String, val lastName: String)

    data class Employee(
            @get:JsonUnwrapped val name: Name,
            val position: String
    )

    @Test
    fun testGithub50UnwrappedError() {
        val json = """{"firstName":"John","lastName":"Smith","position":"Manager"}"""
        expectFailure<InvalidDefinitionException>("GitHub #50 has been fixed!") {
            val obj: Employee = jacksonObjectMapper().readValue(json)
        }
    }
}
