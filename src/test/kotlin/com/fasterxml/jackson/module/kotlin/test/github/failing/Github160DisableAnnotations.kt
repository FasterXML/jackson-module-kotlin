package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.Ignore
import org.junit.Test

class TestGithub160 {
    data class DataClass(val blah: String)

    @Test
    @Ignore("No longer sensical in 3.0; throws IllegalStateException in KotlinModule")
    fun dataClass() {
        val mapper = jacksonMapperBuilder()
            .configure(MapperFeature.USE_ANNOTATIONS, false)
            .build()
        expectFailure<MismatchedInputException>("GitHub #160 has been fixed!") {
            mapper.readValue<DataClass>("""{"blah":"blah"}""")
        }
    }
}
