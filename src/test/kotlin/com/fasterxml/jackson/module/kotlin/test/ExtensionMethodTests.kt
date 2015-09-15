package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

public class TestExtensionMethods {
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
}