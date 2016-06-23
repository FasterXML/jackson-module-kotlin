package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CustomTypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import kotlin.reflect.KParameter

private data class Github32TestObj(val firstName: String, val lastName: String)

class Github32 {

    @Rule
    @JvmField
    var thrown : ExpectedException = ExpectedException.none()

    fun missingFirstNameParameter() = missingConstructorParam(::Github32TestObj.parameters[0])

    fun missingConstructorParam(param: KParameter) = object : CustomTypeSafeMatcher<MissingKotlinParameterException>("MissingKotlinParameterException with missing `${param.name}` parameter") {
        override fun matchesSafely(e: MissingKotlinParameterException): Boolean {
            return e.parameter.equals(param)
        }
    }

    @Test fun `valid mandatory data class constructor param`() {
        jacksonObjectMapper().readValue<Github32TestObj>("""{"firstName": "James", "lastName": "Bond"}""")
    }

    @Test fun `missing mandatory data class constructor param`() {
        thrown.expect(missingFirstNameParameter())
        jacksonObjectMapper().readValue<Github32TestObj>("""{"lastName": "Bond"}""")
    }

    @Test fun `null mandatory data class constructor param`() {
        thrown.expect(missingFirstNameParameter())
        jacksonObjectMapper().readValue<Github32TestObj>("""{"firstName": null, "lastName": "Bond"}""")
    }

}
