package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonMerge
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.databind.node.ObjectNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub211 {
    @Test
    fun simple() {
        val original = Person("original", Address("Rivoli", "Paris"))
        val changes = JsonNodeFactory.instance.objectNode().put("username", "updated")

        val merged = JsonMerger(_root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper()).merge(original, changes)

        assertEquals(original.copy(username = "updated"), merged)
    }

    @Test
    fun nested() {
        val original = Person("original", Address("Rivoli", "Paris"))

        val merged = JsonMerger(_root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper()).merge(original, nestedChanges())

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
