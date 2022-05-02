package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import org.junit.Test
import kotlin.test.assertEquals

class GithubIssue563 {
    @JvmInline
    value class ValueString(val v: String)
    @JvmInline
    value class ValueInt(val v: Int)

    @Test
    fun jsonValueClassNullableString() {
        data class Data(
            val nullableValueString: ValueString?,
            val nullableValueInt: ValueInt?,
            val nullableMember: String?,
        )
        assertEquals(
            "{}",
            jacksonMapperBuilder()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build()
                .writeValueAsString(Data(null, null, null)),
        )
    }

}