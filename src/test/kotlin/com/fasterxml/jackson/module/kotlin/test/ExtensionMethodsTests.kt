package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.*
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
}