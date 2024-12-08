package tools.jackson.module.kotlin.test.github

import kotlin.test.assertEquals
import com.fasterxml.jackson.annotation.JsonMerge
import org.junit.jupiter.api.Test
import tools.jackson.databind.JsonNode
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.databind.node.ObjectNode

import tools.jackson.module.kotlin.jacksonMapperBuilder

class TestGithub211 {
    val mapperWithFinalFieldsAsMutators = jacksonMapperBuilder()
        .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
        .build()

    @Test
    fun simple() {
        val original = Person("original", Address("Rivoli", "Paris"))
        val changes = JsonNodeFactory.instance.objectNode().put("username", "updated")

        val merged = JsonMerger(mapperWithFinalFieldsAsMutators).merge(original, changes)

        assertEquals(original.copy(username = "updated"), merged)
    }

    @Test
    fun nested() {
        val original = Person("original", Address("Rivoli", "Paris"))

        val merged = JsonMerger(mapperWithFinalFieldsAsMutators).merge(original, nestedChanges())

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
