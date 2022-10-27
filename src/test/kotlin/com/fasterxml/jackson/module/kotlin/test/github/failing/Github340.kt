package com.fasterxml.jackson.module.kotlin.test.github.failing

import tools.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.Test
import kotlin.test.assertEquals

class OwnerRequestTest {
    private val jackson = jacksonObjectMapper()

    val json = """{"foo": "Got a foo"}"""

    class NoIsField(val foo: String? = null)

    class IsField(val foo: String? = null) {
        val isFoo = foo != null
    }

    @Test
    fun testDeserHit340() {
        expectFailure<UnrecognizedPropertyException>("GitHub #340 has been fixed!") {
            val value: IsField = jackson.readValue(json)
            assertEquals("Got a foo", value.foo)
        }
    }

    @Test
    fun testDeserWithoutIssue() {
        val value: NoIsField = jackson.readValue(json)
        assertEquals("Got a foo", value.foo)
    }
}
