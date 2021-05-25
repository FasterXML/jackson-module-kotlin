package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertSame

/**
 * An empty object should be deserialized as *the* Unit instance
 */
class TestGithub196 {
    @Test
    fun testUnitSingletonDeserialization() {
        assertSame(jacksonObjectMapper().readValue("{}"), Unit)
    }
}
