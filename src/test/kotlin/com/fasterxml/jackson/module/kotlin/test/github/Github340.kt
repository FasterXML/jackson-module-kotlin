package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OwnerRequestTest {
    private val jackson = ObjectMapper().registerModule(kotlinModule())

    val json = """{"foo": "Got a foo"}"""

    class NoIsField(val foo: String? = null)

    class IsField(val foo: String? = null) {
        val isFoo = foo != null
    }

    @Test
    fun testDeserHit340() {
        val value: IsField = jackson.readValue(json)
        // Fixed
        assertEquals("Got a foo", value.foo)
    }

    @Test
    fun testDeserWithoutIssue() {
        val value: NoIsField = jackson.readValue(json)
        assertEquals("Got a foo", value.foo)
    }

    // A test case for isSetter to work, added with the fix for this issue.
    class IsSetter {
        lateinit var isFoo: String
    }

    @Test
    fun isSetterTest() {
        val json = """{"isFoo":"bar"}"""
        val isSetter: IsSetter = jackson.readValue(json)

        assertEquals("bar", isSetter.isFoo)
    }
}
