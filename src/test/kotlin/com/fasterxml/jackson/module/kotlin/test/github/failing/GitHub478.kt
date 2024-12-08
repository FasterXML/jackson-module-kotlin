package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.ComparisonFailure
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GitHub478Test {
    val mapper = jsonMapper {
        addModule(kotlinModule())
        serializationInclusion(NON_DEFAULT)
    }

    data class Data(val flag: Boolean = true)

    @Test
    fun omitsDefaultValueWhenSerializing() {
        expectFailure<ComparisonFailure>("GitHub478 has been fixed!") {
            assertEquals("""{}""", mapper.writeValueAsString(Data()))
        }
    }

    @Test
    fun serializesNonDefaultValue() {
        expectFailure<ComparisonFailure>("GitHub478 has been fixed!") {
            assertEquals("""{"flag": false}""", mapper.writeValueAsString(Data(flag = false)))
        }
    }

    @Test
    fun usesDefaultWhenDeserializing() {
        assertEquals(Data(), mapper.readValue("{}"))
    }
}
