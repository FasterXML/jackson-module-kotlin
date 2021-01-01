package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.NullInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TestExtensionMethods {
    val mapper: ObjectMapper = jacksonObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, false)

    @Test
    fun testAllInferenceForms() {
        data class BasicPerson(val name: String, val age: Int)

        val json = """{"name":"John Smith","age":30}"""

        val inferRightSide = mapper.readValue<BasicPerson>(json)
        val inferLeftSide: BasicPerson = mapper.readValue(json)
        val person = mapper.readValue<BasicPerson>(json)

        val expectedPerson = BasicPerson("John Smith", 30)

        assertThat(inferRightSide, equalTo(expectedPerson))
        assertThat(inferLeftSide, equalTo(expectedPerson))
        assertThat(person, equalTo(expectedPerson))
    }

    /**
     * https://stackoverflow.com/questions/33368328/how-to-use-jackson-to-deserialize-to-kotlin-collections
     */
    @Test
    fun testStackOverflow33368328() {
        data class MyData(val a: String, val b: Int)

        val jsonStr = """[{"a": "value1", "b": 1}, {"a": "value2", "b": 2}]"""
        val myList: List<MyData> = mapper.readValue(jsonStr)
        assertThat(myList, equalTo(listOf(MyData("value1", 1), MyData("value2", 2))))
    }

    enum class Options { ONE, TWO }

    @Test
    fun testNullEnumThrows() {
        assertThrows(NullInputException::class.java) {
            val foo: Options = mapper.readValue("null")
            assertNull(foo)
        }
    }
}
