package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test

class TestGithub104 {
    abstract class SuperClass(val name: String)

    class SubClass(name: String) : SuperClass(name)
    // note this would fail if the constructor parameter is not named the same as the property


    @Test
    fun testIt() {
        val objectMapper = jacksonObjectMapper()

        val jsonValue = """{"name":"TestName"}"""

        objectMapper.readValue<SubClass>(jsonValue)
    }
}