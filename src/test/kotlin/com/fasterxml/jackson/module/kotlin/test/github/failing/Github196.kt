package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.Test
import kotlin.test.assertSame

class TestGithub196 {
    @Test
    fun testUnitSingletonDeserialization() {
        // An empty object should be deserialized as *the* Unit instance, but is not
        expectFailure<AssertionError>("GitHub #196 has been fixed!") {
            assertSame(jacksonObjectMapper().readValue("{}"), Unit)
        }
    }
}
