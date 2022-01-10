package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import java.util.*

class TestJacksonWithKotlin_1_6 {

    private val normalCasedJson = """{"name":"Frank","age":30,"primaryAddress":"something here","renamed":true,"createdDt":"2016-10-25T18:25:48.000+00:00"}"""
    private val normalCasedMapper = jacksonObjectMapper()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.INDENT_OUTPUT, false)

    private interface TestFields_1_6 {
        val name: String
        val age: Int
        val primaryAddress: String
        val wrongName: Boolean
        val createdDt: Date

        fun validate(
            nameField: String = name,
            ageField: Int = age,
            addressField: String = primaryAddress,
            wrongNameField: Boolean = wrongName,
            createDtField: Date = createdDt
        ) {
            MatcherAssert.assertThat(nameField, CoreMatchers.equalTo("Frank"))
            MatcherAssert.assertThat(ageField, CoreMatchers.equalTo(30))
            MatcherAssert.assertThat(addressField, CoreMatchers.equalTo("something here"))
            MatcherAssert.assertThat(wrongNameField, CoreMatchers.equalTo(true))
            MatcherAssert.assertThat(createDtField, CoreMatchers.equalTo(Date(1477419948000)))
        }
    }

    private class StateObjectWithFactoryOnNamedCompanion_1_6 private constructor(
        override val name: String,
        override val age: Int,
        override val primaryAddress: String,
        override val wrongName: Boolean,
        override val createdDt: Date
    ) : TestFields_1_6 {
        var factoryUsed: Boolean = false

        private companion object Named {
            /**
             * Private to test with 1.6.
             */
            @JvmStatic @JsonCreator private fun create(
                @JsonProperty("name") nameThing: String,
                @JsonProperty("age") age: Int,
                @JsonProperty("primaryAddress") primaryAddress: String,
                @JsonProperty("renamed") wrongName: Boolean,
                @JsonProperty("createdDt") createdDt: Date
            ): StateObjectWithFactoryOnNamedCompanion_1_6 {
                val obj = StateObjectWithFactoryOnNamedCompanion_1_6(nameThing, age, primaryAddress, wrongName, createdDt)
                obj.factoryUsed = true
                return obj
            }
        }
    }

    @Test
    fun findingFactoryMethod() {
        val stateObj = normalCasedMapper.readValue(normalCasedJson, StateObjectWithFactoryOnNamedCompanion_1_6::class.java)
        stateObj.validate()
        MatcherAssert.assertThat(stateObj.factoryUsed, CoreMatchers.equalTo(true))
    }

}
