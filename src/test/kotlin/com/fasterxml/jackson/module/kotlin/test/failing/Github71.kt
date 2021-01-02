package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestGithub71 {
    open class Identifiable {
        internal var identity: Long? = null
    }

    @Test
    fun testInternalPropertySerliazation() {
        val json = jacksonObjectMapper().writeValueAsString(Identifiable())

        try {
            assertEquals("{\"identity\":null}", json) // fails: {"identity$jackson_module_kotlin":null}
            val newInstance = jacksonObjectMapper().readValue<Identifiable>(json)
            assertEquals(Identifiable(), newInstance)
            fail("GitHub #71 has been fixed!")
        } catch (e: AssertionError) {
            // Remove this try/catch and the `fail()` call above when this issue is fixed
        }
    }
}
