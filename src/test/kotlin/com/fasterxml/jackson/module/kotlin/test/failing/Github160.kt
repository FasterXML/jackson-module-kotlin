package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test

class TestGithub160 {
    data class DataClass(val blah: String)

    @Test
//    @Ignore("This error is caused by annotations being turned off, but in Jackson 2.x we cannot catch this uniformly across the board")
    fun dataClass() {
        val j = jacksonMapperBuilder().disable(MapperFeature.USE_ANNOTATIONS
        ).build()!!
        j.readValue<DataClass>(""" {"blah":"blah"}""")
    }
}