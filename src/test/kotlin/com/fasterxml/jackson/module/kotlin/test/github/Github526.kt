package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class Github526 {
    data class D(@JsonSetter(nulls = Nulls.SKIP) val v: Int = -1)

    @Test
    fun test() {
        val mapper = jacksonObjectMapper()
        val d = mapper.readValue<D>("""{"v":null}""")

        assertEquals(-1, d.v)
    }
}
