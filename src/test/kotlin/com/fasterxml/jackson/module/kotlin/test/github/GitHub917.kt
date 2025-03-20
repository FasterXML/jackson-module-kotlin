package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.OptBoolean
import com.fasterxml.jackson.databind.exc.InvalidNullException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class GitHub917 {
    data class Failing<T>(val data: T)

    val mapper = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)

    @Test
    fun failing() {
        val value = Failing<String?>(null)
        val json = mapper.writeValueAsString(value)

        assertThrows<InvalidNullException> {
            val deserializedValue = mapper.readValue<Failing<String?>>(json)
            assertEquals(value ,deserializedValue)
        }
    }

    data class WorkAround<T>(@JsonProperty(isRequired = OptBoolean.FALSE) val data: T)

    @Test
    fun workAround() {
        val value = WorkAround<String?>(null)
        val json = mapper.writeValueAsString(value)

        val deserializedValue = mapper.readValue<WorkAround<String?>>(json)
        assertEquals(value ,deserializedValue)
    }
}
