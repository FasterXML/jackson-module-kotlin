package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.fail

class TestGithub50 {
    data class Name(val firstName: String, val lastName: String)

    data class Employee(
            @get:JsonUnwrapped val name: Name,
            val position: String
    )

    @Test
    fun testGithub50UnwrappedError() {
        val json = """{"firstName":"John","lastName":"Smith","position":"Manager"}"""
        try {
            val obj: Employee = jacksonObjectMapper().readValue(json)
            fail("GitHub #50 has been fixed!")
        } catch (e: InvalidDefinitionException) {
            // Remove this try/catch and the `fail()` call above when this issue is fixed
        }
    }
}
