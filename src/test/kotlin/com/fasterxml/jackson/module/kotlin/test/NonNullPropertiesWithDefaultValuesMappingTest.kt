package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class NonNullPropertiesWithDefaultValuesMappingTest {
    data class TestCaseClass(
            val string: String = "DEFAULT_VAL",
            val javaInteger: Integer = Integer(11),
            val kotlinInt: Int = 12
    )

    @Rule
    @JvmField
    val exception: ExpectedException = ExpectedException.none()

    @Test fun testFullJSON() {
        val json = """{"string":"STRING_VAL","javaInteger":1,"kotlinInt":2}"""

        val result: TestCaseClass = jacksonObjectMapper().readValue(json)

        assertThat(result, CoreMatchers.equalTo(TestCaseClass("STRING_VAL", Integer(1), 2)))
    }

    @Test fun testMissingStringProperty() {
        val json = """{"javaInteger":1,"kotlinInt":2}"""

        val result: TestCaseClass = jacksonObjectMapper().readValue(json)

        assertThat(result, CoreMatchers.equalTo(TestCaseClass("DEFAULT_VAL", Integer(1), 2)))
    }

    @Test fun testMissingJavaIntegerProperty() {
        val json = """{"string":"STRING_VAL","kotlinInt":2}"""

        val result: TestCaseClass = jacksonObjectMapper().readValue(json)

        assertThat(result, CoreMatchers.equalTo(TestCaseClass("STRING_VAL", Integer(11), 2)))
    }

    @Test fun testMissingKotlinIntProperty() {
        val json = """{"string":"STRING_VAL","javaInteger":1}"""

        val result: TestCaseClass = jacksonObjectMapper().readValue(json)

        assertThat(result, CoreMatchers.equalTo(TestCaseClass("STRING_VAL", Integer(1), 12)))
    }

    @Test fun testMissingAllProperties() {
        val json = "{}"

        val result: TestCaseClass = jacksonObjectMapper().readValue(json)

        assertThat(result, CoreMatchers.equalTo(TestCaseClass("DEFAULT_VAL", Integer(11), 12)))
    }
}