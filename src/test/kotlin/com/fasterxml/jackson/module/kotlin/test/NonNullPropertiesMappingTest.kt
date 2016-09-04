package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class NonNullPropertiesMappingTest {
    data class TestCaseClass(
            val string: String,
            val javaInteger: Integer,
            val kotlinInt: Int
    )

    @Rule
    @JvmField
    val exception: ExpectedException = ExpectedException.none()

    @Test fun testFullJSON() {
        val json = """{"string":"STRING_VAL","javaInteger":1,"kotlinInt":2}"""

        val result: TestCaseClass = jacksonObjectMapper().readValue(json)

        assertThat(result, equalTo(TestCaseClass("STRING_VAL", Integer(1), 2)))
    }

    @Test fun testMissingStringProperty() {
        expectMissingParam()
        val json = """{"javaInteger":1,"kotlinInt":2}"""

        jacksonObjectMapper().readValue<TestCaseClass>(json)
    }

    @Test fun testMissingJavaIntegerProperty() {
        expectMissingParam()
        val json = """{"string":"STRING_VAL","kotlinInt":2}"""

        jacksonObjectMapper().readValue<TestCaseClass>(json)
    }

    @Test fun testMissingKotlinIntProperty() {
        expectMissingParam()
        val json = """{"string":"STRING_VAL","javaInteger":1}"""

        jacksonObjectMapper().readValue<TestCaseClass>(json)
    }

    @Test fun testMissingAllProperty() {
        expectMissingParam()
        val json = "{}"

        jacksonObjectMapper().readValue<TestCaseClass>(json)
    }

    private fun expectMissingParam() {
        exception.expect(JsonMappingException::class.java)
    }
}