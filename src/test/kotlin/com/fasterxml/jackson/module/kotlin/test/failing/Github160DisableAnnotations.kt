package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.fail

class TestGithub160 {
    data class DataClass(val blah: String)

    @Test
    fun dataClass() {
        val mapper = jacksonObjectMapper().configure(MapperFeature.USE_ANNOTATIONS, false)
        try {
            mapper.readValue<DataClass>("""{"blah":"blah"}""")
            fail("GitHub #160 has been fixed!")
        } catch (e: MismatchedInputException) {
            // Remove this try/catch and the `fail()` call above when this issue is fixed
        }
    }
}
