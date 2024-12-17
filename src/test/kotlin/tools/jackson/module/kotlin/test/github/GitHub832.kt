package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.test.Test
import kotlin.test.assertEquals

class GitHub832 {
    data class AnySetter @JvmOverloads constructor(
        val test: String? = null,
        @JsonAnySetter
        @get:JsonAnyGetter
        val anything: Map<String, Any?> = mutableMapOf(),
    )

    @Test
    fun testDeserialization() {
        val json = """
            {
                "widget": {
                    "debug": "on"
                 }
             }     """.trimMargin()
        val jacksonMapper = ObjectMapper()
        jacksonMapper.registerModules(KotlinModule.Builder().build())
        val anySetter = jacksonMapper.readValue<AnySetter>(json)
        assertEquals("widget", anySetter.anything.entries.first().key)
    }
}
