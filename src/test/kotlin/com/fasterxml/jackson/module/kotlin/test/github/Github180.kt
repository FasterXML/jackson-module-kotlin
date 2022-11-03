package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertNull

class TestGithub180 {
    class TestClass(val instantName: String? = null, val someInt: Int? = null) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun create(
                    @JsonProperty("instantName") instantName: String?,
                    @JsonProperty("someInt") someInt: Int?
            ): TestClass {
                return TestClass(instantName, someInt)
            }
        }

    }

    @Test
    fun testMissingProperty() {
        val obj = jacksonObjectMapper().readValue<TestClass>("""{}""")
        assertNull(obj.instantName)
        assertNull(obj.someInt)
    }
}
