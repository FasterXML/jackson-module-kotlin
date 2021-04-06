package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonMerge
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub211 {
    @Test
    fun simple() {
        val original = Person("original", Address("Rivoli", "Paris"))
        val changes = JsonNodeFactory.instance.objectNode().put("username", "updated")

        val merged = JsonMerger(jacksonObjectMapper()).merge(original, changes)

        assertEquals(original.copy(username = "updated"), merged)
    }

    @Test
    fun nested() {
        val original = Person("original", Address("Rivoli", "Paris"))

        val merged = JsonMerger(jacksonObjectMapper()).merge(original, nestedChanges())

        assertEquals(Person("updated", Address("Magenta", "Paris")), merged)
    }

    private fun nestedChanges(): ObjectNode {
        return JsonNodeFactory.instance.objectNode().put("username", "updated").apply {
            putObject("address").put("street", "Magenta")
        }
    }

    class JsonMerger(private val objectMapper: ObjectMapper) {
        fun <T> merge(toUpdate: T, changes: JsonNode): T {
            return objectMapper.readerForUpdating(toUpdate).readValue(changes)
        }
    }

    data class Address(val street: String, val city: String)

    data class Person(val username: String, @JsonMerge val address: Address)
}
