package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class Github354 {
    data class CreatorWithoutJvmStatic(val value: String) {
        companion object {
            @JsonCreator
            fun create(value: String) = CreatorWithoutJvmStatic("Created by creator with value $value")
        }
    }

    @Test
    fun jsonCreatorMethodWithoutJvmStatic() {
        val objectMapper = jacksonObjectMapper()
        val testValue = "some-value"

        assertEquals(CreatorWithoutJvmStatic.create(testValue), objectMapper.readValue("""{"value": "$testValue"}"""))
    }

    data class CreatorWithJvmStatic(val value: String) {
        companion object {
            @JsonCreator
            @JvmStatic
            fun create(value: String) = CreatorWithJvmStatic("Created by creator with value $value")
        }
    }

    @Test
    fun jsonCreatorWithJvmStatic() {
        val objectMapper = jacksonObjectMapper()
        val testValue = "some-value"

        assertEquals(CreatorWithJvmStatic.create(testValue), objectMapper.readValue("""{"value": "$testValue"}"""))
    }
}
