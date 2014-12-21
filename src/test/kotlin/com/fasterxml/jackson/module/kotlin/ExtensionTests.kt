package com.fasterxml.jackson.module.kotlin

import org.junit.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.equalTo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

data class BasicPerson(val name: String, val age: Int)

class ExtensionTests {
    val json = """{"name":"John Smith", "age":30}"""
    var person: BasicPerson? = null
    val mapper: ObjectMapper = jacksonObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, false)


    Test fun testAllInferenceForms() {
        val inferRightSide = mapper.readValue<BasicPerson>(json)
        val inferLeftSide: BasicPerson = mapper.readValue(json)
        person = mapper.readValue(json)

        val testPerson = BasicPerson("John Smith", 30)

        assertThat(inferRightSide, equalTo(testPerson))
        assertThat(inferLeftSide, equalTo(testPerson))
        assertThat(person, equalTo(testPerson))
    }
}