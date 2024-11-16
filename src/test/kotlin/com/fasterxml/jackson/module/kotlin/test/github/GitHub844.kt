package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.test.Test
import kotlin.test.assertEquals

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
private sealed class BaseClass

private data class ChildClass(val text: String) : BaseClass()

class GitHub844 {
    @Test
    fun test() {
        val json = """
        {
            "_type": "ChildClass",
            "text": "Test"
        }
        """

        val jacksonObjectMapper = ObjectMapper().registerKotlinModule()
        val message = jacksonObjectMapper.readValue<BaseClass>(json)

        assertEquals(ChildClass("Test"), message)
    }
}
