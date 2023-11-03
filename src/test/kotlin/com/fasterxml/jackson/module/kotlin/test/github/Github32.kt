package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CustomTypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import kotlin.reflect.KParameter


class TestGithub32 {

    @Rule
    @JvmField
    var thrown: ExpectedException = ExpectedException.none()

    @Test fun `valid mandatory data class constructor param`() {
        jacksonObjectMapper().readValue<Person>("""
        {
            "firstName": "James",
            "lastName": "Bond"
        }
        """.trimIndent())
    }

    @Test fun `missing mandatory data class constructor param`() {
        thrown.expect(missingFirstNameParameter())
        thrown.expect(pathMatches("firstName"))
        thrown.expect(location(line = 3, column = 1))
        jacksonObjectMapper().readValue<Person>("""
        {
            "lastName": "Bond"
        }
        """.trimIndent())
    }

    @Test fun `null mandatory data class constructor param`() {
        thrown.expect(missingFirstNameParameter())
        thrown.expect(pathMatches("firstName"))
        thrown.expect(location(line = 4, column = 1))
        jacksonObjectMapper().readValue<Person>("""
        {
            "firstName": null,
            "lastName": "Bond"
        }
        """.trimIndent())
    }

    @Test fun `missing mandatory constructor param - nested in class with default constructor`() {
        thrown.expect(missingFirstNameParameter())
        thrown.expect(pathMatches("person.firstName"))
        thrown.expect(location(line = 4, column = 5))
        jacksonObjectMapper().readValue<WrapperWithDefaultContructor>("""
        {
            "person": {
                "lastName": "Bond"
            }
        }
        """.trimIndent())
    }

    @Test fun `missing mandatory constructor param - nested in class with single arg constructor`() {
        thrown.expect(missingFirstNameParameter())
        thrown.expect(pathMatches("person.firstName"))
        thrown.expect(location(line = 4, column = 5))
        jacksonObjectMapper().readValue<WrapperWithArgsContructor>("""
        {
            "person": {
                "lastName": "Bond"
            }
        }
        """.trimIndent())
    }

    @Test fun `missing mandatory constructor param - nested in class with List arg constructor`() {
        thrown.expect(missingFirstNameParameter())
        thrown.expect(pathMatches("people[0].firstName"))
        thrown.expect(location(line = 7, column = 9))
        jacksonObjectMapper().readValue<Crowd>("""
        {
            "people": [
                {
                    "person": {
                        "lastName": "Bond"
                    }
                }
            ]
        }
        """.trimIndent())
    }

}


private data class Person(val firstName: String, val lastName: String)

private data class WrapperWithArgsContructor(val person: Person)

private data class WrapperWithDefaultContructor(val person: Person? = null)

private data class Crowd(val people: List<Person>)


private fun missingFirstNameParameter() = missingConstructorParam(::Person.parameters[0])

private fun missingConstructorParam(param: KParameter) = object : CustomTypeSafeMatcher<MissingKotlinParameterException>("MissingKotlinParameterException with missing `${param.name}` parameter") {
    override fun matchesSafely(e: MissingKotlinParameterException): Boolean = e.parameter.equals(param)
}

private fun pathMatches(path: String) = object : CustomTypeSafeMatcher<MissingKotlinParameterException>("MissingKotlinParameterException with path `$path`") {
    override fun matchesSafely(e: MissingKotlinParameterException): Boolean = e.getHumanReadablePath().equals(path)
}

private fun location(line: Int, column: Int) = object : CustomTypeSafeMatcher<MissingKotlinParameterException>("MissingKotlinParameterException with location (line=$line, column=$column)") {
    override fun matchesSafely(e: MissingKotlinParameterException): Boolean {
        return e.location != null && line.equals(e.location.lineNr) && column.equals(e.location.columnNr)
    }
}

private fun JsonMappingException.getHumanReadablePath(): String {
    val builder = StringBuilder()
    this.path.forEachIndexed { i, reference ->
        if (reference.index >= 0) {
            builder.append("[${reference.index}]")
        } else {
            if (i > 0) builder.append(".")
            builder.append("${reference.fieldName}")
        }
    }
    return builder.toString()
}