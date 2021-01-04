package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.Test

class TestGithub160 {
    data class DataClass(val blah: String)

    @Test
    fun dataClass() {
        val mapper = jacksonObjectMapper().configure(MapperFeature.USE_ANNOTATIONS, false)
        expectFailure<MismatchedInputException>("GitHub #160 has been fixed!") {
            mapper.readValue<DataClass>("""{"blah":"blah"}""")
        }
    }
}
