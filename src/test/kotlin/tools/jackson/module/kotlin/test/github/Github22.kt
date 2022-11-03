package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub22 {
    class StringValue constructor(s: String) {
        val other: String = s

        @JsonValue override fun toString() = other
    }

    /*
    data class StringValue2(@get:JsonIgnore val s: String) {
        @JsonValue override fun toString() = s
    }
    */

    @Test fun testJsonValueNoMatchingMemberWithConstructor() {
        val expectedJson = "\"test\""
        val expectedObj = StringValue("test")

        val actualJson = jacksonObjectMapper().writeValueAsString(expectedObj)
        assertEquals(expectedJson, actualJson)

        val actualObj = jacksonObjectMapper().readValue<StringValue>("\"test\"")
        assertEquals(expectedObj.other, actualObj.other)

    }

    // 28-Dec-2019, tatu: Regression with 3.0, moved to a new separate test class.
    /*
    @Test fun testJsonValue2DataClassIgnoredMemberInConstructor() {
        val expectedJson = "\"test\""
        val expectedObj = StringValue2("test")

        val actualJson = jacksonObjectMapper().writeValueAsString(expectedObj)
        assertEquals(expectedJson, actualJson)

        val actualObj = jacksonObjectMapper().readValue<StringValue2>("\"test\"")
        assertEquals(expectedObj, actualObj)

    }
     */
}