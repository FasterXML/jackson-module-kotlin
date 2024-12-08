package tools.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonIgnore
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.module.kotlin.addMixIn
import tools.jackson.module.kotlin.contains
import tools.jackson.module.kotlin.convertValue
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.minusAssign
import tools.jackson.module.kotlin.plusAssign
import tools.jackson.module.kotlin.readValue
import tools.jackson.module.kotlin.treeToValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private class TestExtensionMethods {
    val mapper: ObjectMapper = jacksonMapperBuilder().disable(SerializationFeature.INDENT_OUTPUT)
        .build()

    data class BasicPerson(val name: String, val age: Int)

    @Test
    fun testAllInferenceForms() {
        val json = """{"name":"John Smith","age":30}"""

        val inferRightSide = mapper.readValue<BasicPerson>(json)
        val inferLeftSide: BasicPerson = mapper.readValue(json)
        val person = mapper.readValue<BasicPerson>(json)

        val expectedPerson = BasicPerson("John Smith", 30)

        assertEquals(expectedPerson, inferRightSide)
        assertEquals(expectedPerson, inferLeftSide)
        assertEquals(expectedPerson, person)
    }

    data class MyData(val a: String, val b: Int)

    @Test
    fun testStackOverflow33368328() {
        val jsonStr = """[{"a": "value1", "b": 1}, {"a": "value2", "b": 2}]"""
        val myList: List<MyData> = mapper.readValue(jsonStr)
        assertEquals(listOf(MyData("value1", 1), MyData("value2", 2)), myList)
    }

    @Test
    fun testOperatorFunExtensions() {
        val factory = JsonNodeFactory.instance

        val objectNode = factory.objectNode()
        objectNode.put("foo1", "bar")
        objectNode.put("foo2", "baz")
        objectNode.put("foo3", "bah")
        objectNode -= "foo1"
        objectNode -= listOf("foo2")

        assertTrue("foo1" !in objectNode)
        assertTrue("foo3" in objectNode)

        val arrayNode = factory.arrayNode()
        arrayNode += "foo"
        arrayNode += true
        arrayNode += 1
        arrayNode += 1.0
        arrayNode += "bar".toByteArray()

        assertEquals(5, arrayNode.size())

        (4 downTo 0).forEach { arrayNode -= it }
        assertEquals(0, arrayNode.size())
    }

    @Test fun noTypeErasure() {
        data class Person(val name: String)

        val source = """[ { "name" : "Neo" } ]"""
        val tree = mapper.readTree(source)

        val readValueResult: List<Person> = mapper.readValue(source)
        assertEquals(listOf(Person("Neo")), readValueResult)

        val treeToValueResult: List<Person> = mapper.treeToValue(tree)
        assertEquals(listOf(Person("Neo")), treeToValueResult)

        val convertValueResult: List<Person> = mapper.convertValue(tree)
        assertEquals(listOf(Person("Neo")), convertValueResult)
    }

    @Test fun mixInExtensionTest() {
        data class Person(val name: String)
        abstract class PersonMixIn {
            @JsonIgnore
            var name: String = ""
        }

        val mapper: JsonMapper = jsonMapper { addMixIn<Person, PersonMixIn>() }
        val serializedPerson: String = mapper.writeValueAsString(Person("test"))

        assertEquals("{}", serializedPerson)
    }
}
