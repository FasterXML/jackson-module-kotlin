package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import java.io.StringWriter
import kotlin.platform.platformStatic
import kotlin.properties.Delegates
import kotlin.test.fail

public class TestJacksonWithKotlin {
    // testing using custom serializer, JodaDateTime to be sure we don't break working with other modules or complex types

    private trait TestFields {
        val name: String
        val age: Int
        val primaryAddress: String
        val wrongName: Boolean
        val createdDt: DateTime
    }

    private fun validate(stateObj: TestFields) {
        assertThat(stateObj.name, equalTo("Frank"))
        assertThat(stateObj.age, equalTo(30))
        assertThat(stateObj.primaryAddress, equalTo("something here"))
        assertThat(stateObj.wrongName, equalTo(true))
        assertThat(stateObj.createdDt, equalTo(DateTime(2014, 8, 1, 12, 11, 30, 0, DateTimeZone.UTC)))
    }

    private val normalCasedJson = """{"name":"Frank","age":30,"primaryAddress":"something here","renamed":true,"createdDt":"2014-08-01T12:11:30.000Z"}"""
    private val pascalCasedJson = """{"Name":"Frank","Age":30,"PrimaryAddress":"something here","Renamed":true,"CreatedDt":"2014-08-01T12:11:30.000Z"}"""

    private val normalCasedMapper = jacksonObjectMapper()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.INDENT_OUTPUT, false)
            .registerModule(JodaModule())


    private val pascalCasedMapper = jacksonObjectMapper()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.INDENT_OUTPUT, false)
            .registerModule(JodaModule())
            .setPropertyNamingStrategy(PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE)

    // ==================

    private class DefaultAndSpecificConstructor(override var name: String = "", override var age: Int = 0) : TestFields {
        [JsonProperty("renamed")]
        override var wrongName: Boolean = false

        override var primaryAddress: String = ""
        override var createdDt: DateTime = DateTime()
    }

    Test fun NoFailWithDefaultAndSpecificConstructor() {
        val stateObj = normalCasedMapper.readValue<DefaultAndSpecificConstructor>(normalCasedJson)
        validate(stateObj)
    }

    // ==================

    private class NoFailWithoutJsonCreator(override val name: String, override val age: Int, override val primaryAddress: String, val renamed: Boolean, override val createdDt: DateTime) : TestFields {
        [JsonIgnore]
        override val wrongName = renamed // here for the test validation only
    }

    Test fun doNotFailWithoutJsonCreator() {
        val stateObj = normalCasedMapper.readValue<NoFailWithoutJsonCreator>(normalCasedJson)
        validate(stateObj)
    }

    // ==================

    private data class StateObjectAsDataClassExplicitJsonCreator [JsonCreator] (override val name: String, override val age: Int, override val primaryAddress: String, val renamed: Boolean, override val createdDt: DateTime) : TestFields {
        [JsonIgnore]
        override val wrongName = renamed // here for the test validation only
    }

    Test fun testDataClassWithExplicitJsonCreator() {
        // data class with explicit JsonCreator and no parameters with JsonProperty
        val stateObj = normalCasedMapper.readValue<StateObjectAsDataClassExplicitJsonCreator>(normalCasedJson)
        validate(stateObj)

        val test1out = StringWriter()
        normalCasedMapper.writeValue(test1out, stateObj)

        assertThat(test1out.getBuffer().toString(), equalTo(normalCasedJson))
    }

    // ==================

    private data class StateObjectAsDataClassWithJsonCreatorAndJsonProperty [JsonCreator] (override val name: String, override val age: Int, override val primaryAddress: String, JsonProperty("renamed") override val wrongName: Boolean, override val createdDt: DateTime) : TestFields

    Test fun testDataClassWithExplicitJsonCreatorAndJsonProperty() {
        // data class with JsonCreator and JsonProperty
        val stateObj = normalCasedMapper.readValue<StateObjectAsDataClassWithJsonCreatorAndJsonProperty>(normalCasedJson)
        validate(stateObj)

        val test1out = normalCasedMapper.writeValueAsString(stateObj)
        assertThat(test1out, equalTo(normalCasedJson))
    }

    // ==================

    private class StateObjectAsNormalClass [JsonCreator] (override val name: String, override val age: Int, override val primaryAddress: String, JsonProperty("renamed") override val wrongName: Boolean, override val createdDt: DateTime) : TestFields

    Test fun testNormalClassWithJsonCreator() {
        // normal class
        val stateObj = normalCasedMapper.readValue<StateObjectAsNormalClass>(normalCasedJson)
        validate(stateObj)
    }

    // ==================

    private class StateObjectWithPartialFieldsInConstructor(override val name: String, override val age: Int, override val primaryAddress: String) : TestFields {
        JsonProperty("renamed") override var wrongName: Boolean = false
        override var createdDt: DateTime by Delegates.notNull()
    }

    Test fun testNormalClassWithPartialConstructorJsonCreator() {
        // normal class with some fields not in constructor
        val stateObj = normalCasedMapper.readValue<StateObjectWithPartialFieldsInConstructor>(normalCasedJson)
        validate(stateObj)
    }

    // ==================

    private data class StateObjectAsDataClassConfusingConstructor(nonField: String?, override val name: String, yearOfBirth: Int, override val age: Int = DateTime().getYear() - yearOfBirth, override val primaryAddress: String, JsonProperty("renamed") override val wrongName: Boolean, override val createdDt: DateTime) : TestFields

    Test fun testDataClassWithNonFieldParametersInConstructor() {
        // data class with non fields appearing as parameters in constructor, this works but null values or defaults for primitive types are passed to
        // the unrecognized fields in the constructor.  Does not work with default values for parameters, because a null does not get converted to the
        // default.

        val stateObj = normalCasedMapper.readValue<StateObjectAsDataClassConfusingConstructor>(normalCasedJson)
        validate(stateObj)
    }

    // ==================

    Test fun findingConstructorsWithPascalCasedJson() {
        // pascal cased strategy for JSON, note that explicit named JsonProperty are not renamed and must be exactly the same
        val stateObj = pascalCasedMapper.readValue<StateObjectAsDataClassExplicitJsonCreator>(pascalCasedJson)
        validate(stateObj)

        val test1out = pascalCasedMapper.writeValueAsString(stateObj)
        assertThat(test1out, equalTo(pascalCasedJson))
    }


    // ==================

    private class StateObjectWithFactory private (namey: String, agey: Int, primaryAddressy: String, wrongNamey: Boolean, createdDty: DateTime) : TestFields {
        companion object {
            [platformStatic] public [JsonCreator] fun create([JsonProperty("name")] nameThing: String, [JsonProperty("age")] age: Int, [JsonProperty("primaryAddress")] primaryAddress: String, [JsonProperty("renamed")] wrongName: Boolean, [JsonProperty("createdDt")] createdDt: DateTime): StateObjectWithFactory {
                return StateObjectWithFactory(nameThing, age, primaryAddress, wrongName, createdDt)
            }
        }
        override val name = namey
        override val age = agey
        override val primaryAddress = primaryAddressy
        override val wrongName = wrongNamey
        override val createdDt = createdDty
    }

    Test fun findingFactoryMethod() {
        val stateObj = normalCasedMapper.readValue(normalCasedJson, javaClass<StateObjectWithFactory>())
        validate(stateObj)
    }

    private class StateObjectWithFactoryNoParamAnnotations(val name: String, val age: Int, val primaryAddress: String, val renamed: Boolean, val createdDt: DateTime) {
        companion object {
            [platformStatic] public [JsonCreator] fun create(name: String, age: Int, primaryAddress: String, renamed: Boolean, createdDt: DateTime): StateObjectWithFactoryNoParamAnnotations {
                return StateObjectWithFactoryNoParamAnnotations(name, age, primaryAddress, renamed, createdDt)
            }
        }
    }

    Test fun findingFactoryMethod2() {
        try {
            val stateObj = normalCasedMapper.readValue(normalCasedJson, javaClass<StateObjectWithFactoryNoParamAnnotations>())
        }
        catch (ex: Exception) {
            fail("Exception not expected")
        }
    }
}

