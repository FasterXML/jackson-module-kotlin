package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.module.kotlin.addMixIn
import com.fasterxml.jackson.module.kotlin.contains
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.minusAssign
import com.fasterxml.jackson.module.kotlin.plusAssign
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TestExtensionMethods {
    val mapper: ObjectMapper = jacksonObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, false)

    data class BasicPerson(val name: String, val age: Int)

    @Test fun testAllInferenceForms() {
        val json = """{"name":"John Smith","age":30}"""

        val inferRightSide = mapper.readValue<BasicPerson>(json)
        val inferLeftSide: BasicPerson = mapper.readValue(json)
        val person = mapper.readValue<BasicPerson>(json)

        val expectedPerson = BasicPerson("John Smith", 30)

        assertThat(inferRightSide, equalTo(expectedPerson))
        assertThat(inferLeftSide, equalTo(expectedPerson))
        assertThat(person, equalTo(expectedPerson))
    }

    data class MyData(val a: String, val b: Int)

    @Test fun testStackOverflow33368328() {
        val jsonStr = """[{"a": "value1", "b": 1}, {"a": "value2", "b": 2}]"""
        val myList: List<MyData> = mapper.readValue(jsonStr)
        assertThat(myList, equalTo(listOf(MyData("value1", 1), MyData("value2", 2))))
    }

    @Test fun testOperatorFunExtensions() {
        val factory = JsonNodeFactory.instance

        val objectNode = factory.objectNode()
        objectNode.put("foo1", "bar")
        objectNode.put("foo2", "baz")
        objectNode.put("foo3", "bah")
        objectNode -= "foo1"
        objectNode -= listOf("foo2")

        assertThat("foo1" !in objectNode, `is`(true))
        assertThat("foo3" in objectNode, `is`(true))

        val arrayNode = factory.arrayNode()
        arrayNode += "foo"
        arrayNode += true
        arrayNode += 1
        arrayNode += 1.0
        arrayNode += "bar".toByteArray()

        assertThat(arrayNode.size(), `is`(5))

        (4 downTo 0).forEach { arrayNode -= it }
        assertThat(arrayNode.size(), `is`(0))
    }

    @Test fun noTypeErasure(){
        data class Person(val name: String)
        val source = """[ { "name" : "Neo" } ]"""
        val tree = mapper.readTree(source)

        val readValueResult: List<Person> = mapper.readValue(source)
        assertThat(readValueResult, `is`(listOf(Person("Neo"))))

        val treeToValueResult: List<Person> = mapper.treeToValue(tree)
        assertThat(treeToValueResult, `is`(listOf(Person("Neo"))))

        val convertValueResult: List<Person> = mapper.convertValue(tree)
        assertThat(convertValueResult, `is`(listOf(Person("Neo"))))
    }

    @Test fun mixInExtensionTest() {

        data class Person(val name: String)
        abstract class PersonMixIn { @JsonIgnore var name: String = "" }

        val mapper: JsonMapper = jsonMapper { addMixIn<Person, PersonMixIn>() }
        val serializedPerson: String = mapper.writeValueAsString(Person("test"))

        assertThat(serializedPerson, `is`("{}"))
    }
}
