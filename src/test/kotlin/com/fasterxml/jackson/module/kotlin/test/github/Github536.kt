package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonKey
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.testPrettyWriter
import junit.framework.TestCase.assertEquals
import org.junit.Test

class Github536 {
    @JvmInline
    value class JsonKeyGetter(val value: Int) {
        @get:JsonKey
        val jsonKey: String
            get() = this.toString()
    }

    interface IJsonKeyGetter {
        @get:JsonKey
        val jsonKey: String
            get() = this.toString()
    }

    @JvmInline
    value class JsonKeyGetterImplementation(val value: Int) : IJsonKeyGetter

    @JvmInline
    value class JsonKeyGetterImplementationDisabled(val value: Int) : IJsonKeyGetter {
        @get:JsonKey(false)
        override val jsonKey: String
            get() = super.jsonKey
    }

    private val writer = jacksonMapperBuilder().build().testPrettyWriter()

    @Test
    fun test() {
        val src = mapOf(
            JsonKeyGetter(0) to 0,
            JsonKeyGetterImplementation(1) to 1,
            JsonKeyGetterImplementationDisabled(2) to 2
        )

        assertEquals(
            """
                {
                  "JsonKeyGetter(value=0)" : 0,
                  "JsonKeyGetterImplementation(value=1)" : 1,
                  "2" : 2
                }
            """.trimIndent(),
            writer.writeValueAsString(src)
        )
    }
}
