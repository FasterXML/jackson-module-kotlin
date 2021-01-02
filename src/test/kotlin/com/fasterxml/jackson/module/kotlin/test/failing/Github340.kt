package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class OwnerRequestTest {
    private val jackson = ObjectMapper().registerModule(KotlinModule())

    val json = """{"foo": "Got a foo"}"""

    class NoIsField(val foo: String? = null)

    class IsField(val foo: String? = null) {
        val isFoo = foo != null
    }

    @Test
    fun testDeserHit340() {
        try {
            val value: IsField = jackson.readValue(json)
            assertEquals("Got a foo", value.foo)
            fail("GitHub #340 has been fixed!")
        } catch (e: UnrecognizedPropertyException) {
            // Remove this try/catch and the `fail()` call above when this issue is fixed
        }
    }

    @Test
    fun testDeserWithoutIssue() {
        val value: NoIsField = jackson.readValue(json)
        assertEquals("Got a foo", value.foo)
    }
}
