package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals

class GitHub451 {
    data class Target(
        val `foo-bar`: String,
        @get:JvmName("getBaz-qux")
        val bazQux: String
    ) {
        fun `getQuux-corge`(): String = `foo-bar`
        @JvmName("getGrault-graply")
        fun getGraultGraply(): String = bazQux
    }

    val mapper = jacksonObjectMapper()

    @Test
    @Ignore
    fun serializeTest() {
        val expected = """{"foo-bar":"a","baz-qux":"b","quux-corge":"a","grault-graply":"b"}"""

        val src = Target("a", "b")
        val json = mapper.writeValueAsString(src)
        assertEquals(expected, json)
    }
}
