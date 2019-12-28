package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub71 {
    open class Identifiable {
        internal var identity: Long? = null
    }

    @Test
    fun testInternalPropertySerliazation() {
        val json = jacksonObjectMapper().writeValueAsString(Identifiable())
        assertEquals("{\"identity\":null}", json) // fails: {"identity$jackson_module_kotlin":null}
        val newInstance = jacksonObjectMapper().readValue<Identifiable>(json)
        assertEquals(Identifiable(), newInstance)
    }
}