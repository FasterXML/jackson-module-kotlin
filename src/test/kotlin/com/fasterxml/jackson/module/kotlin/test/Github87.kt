package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub87 {
    data class DataClassWithDefaults(val bar: String = "default", val baz: String = "default")

    @Test
    fun testCompanionObjectCreatorWithDefaultParameters() {
        assertEquals(DataClassWithDefaults("default", "bazValue"),
            jacksonObjectMapper().readValue("""{"baz": "bazValue", "bar":null}"""))
    }

}