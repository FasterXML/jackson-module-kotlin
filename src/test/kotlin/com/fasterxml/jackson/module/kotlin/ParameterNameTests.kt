package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonProperty
import org.joda.time.DateTime
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import java.io.StringWriter
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import org.junit.Test
import org.hamcrest.MatcherAssert.*
import org.hamcrest.CoreMatchers.*
import org.joda.time.DateTimeZone
import com.fasterxml.jackson.databind.SerializationFeature
import kotlin.properties.Delegates
import com.fasterxml.jackson.annotation.JsonCreator
import kotlin.test.fail
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.annotation.JsonIgnore
import org.junit.Ignore

public class TestJacksonWithKotlin {
    private trait TestFields {
        val name: String
        val age: Int
        val primaryAddress: String
        val wrongName: Boolean
        val createdDt: DateTime
    }

    private class FailWithoutJsonCreator(override val name: String, override val age: Int, override val primaryAddress: String, val renamed: Boolean, override val createdDt: DateTime) : TestFields {
        [JsonIgnore]
        override val wrongName = renamed // here for the test validation only
    }

    private data class StateObjectAsDataClassExplicitJsonCreator [JsonCreator] (override val name: String, override val age: Int, override val primaryAddress: String, val renamed: Boolean, override val createdDt: DateTime) : TestFields {
        [JsonIgnore]
        override val wrongName = renamed // here for the test validation only
    }

    private data class StateObjectAsDataClassWithJsonCreatorAndJsonProperty [JsonCreator] (override val name: String, override val age: Int, override val primaryAddress: String, JsonProperty("renamed") override val wrongName: Boolean, override val createdDt: DateTime) : TestFields

    private class StateObjectAsNormalClass [JsonCreator] (override val name: String, override val age: Int, override val primaryAddress: String, JsonProperty("renamed") override val wrongName: Boolean, override val createdDt: DateTime) : TestFields

    private class StateObjectWithPartialFieldsInConstructor [JsonCreator] (override val name: String, override val age: Int, override val primaryAddress: String) : TestFields {
        JsonProperty("renamed") override var wrongName: Boolean = false
        override var createdDt: DateTime by Delegates.notNull()
    }

    private data class StateObjectAsDataClassConfusingConstructor [JsonCreator] (nonField: String?, override val name: String, yearOfBirth: Int, override val age: Int = DateTime().getYear() - yearOfBirth, override val primaryAddress: String, JsonProperty("renamed") override val wrongName: Boolean, override val createdDt: DateTime) : TestFields

    private class StateObjectWithFactory(val namey: String, val agey: Int, val primaryAddressy: String, val wrongNamey: Boolean, val createdDty: DateTime) : TestFields {
        class object {
            public [JsonCreator] fun create(name: String, age: Int, primaryAddress: String, JsonProperty("renamed") wrongName: Boolean, createdDt: DateTime): StateObjectWithFactory {
                return StateObjectWithFactory(name, age, primaryAddress, wrongName, createdDt)
            }
        }
        override val name = namey
        override val age = agey
        override val primaryAddress = primaryAddressy
        override val wrongName = wrongNamey
        override val createdDt = createdDty
    }

    private val normalCasedJson = """{"name":"Frank","age":30,"primaryAddress":"something here","renamed":true,"createdDt":"2014-08-01T12:11:30.000Z"}"""
    private val pascalCasedJson = """{"Name":"Frank","Age":30,"PrimaryAddress":"something here","Renamed":true,"CreatedDt":"2014-08-01T12:11:30.000Z"}"""

    private val normalCasedMapper = run {
        val mapper: ObjectMapper = ObjectMapper()
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false)
        mapper.registerModule(JodaModule())
        mapper.registerModule(KotlinModule())
        mapper
    }

    private val pascalCasedMapper: ObjectMapper = run {
        val mapper: ObjectMapper = ObjectMapper()
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false)
        mapper.registerModule(JodaModule())
        mapper.registerModule(KotlinModule())
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE);
        mapper
    }

    private fun validate(stateObj: TestFields) {
        assertThat(stateObj.name, equalTo("Frank"))
        assertThat(stateObj.age, equalTo(30))
        assertThat(stateObj.primaryAddress, equalTo("something here"))
        assertThat(stateObj.wrongName, equalTo(true))
        assertThat(stateObj.createdDt, equalTo(DateTime(2014, 8, 1, 12, 11, 30, 0, DateTimeZone.UTC)))
    }

    [Ignore(value = "Failing with the wrong exception currently, see https://github.com/FasterXML/jackson-module-paranamer/issues/11")]
    Test fun failWithoutJsonCreator() {
        try {
            normalCasedMapper.readValue(normalCasedJson, javaClass<FailWithoutJsonCreator>())!!
            fail("without a JsonCreator annotation, the class should fail to find suitable constructor")
        } catch (ex: JsonMappingException) {
            assertThat(ex.getMessage(), containsString("No suitable constructor found"))
        }
    }

    Test fun doNotFailWithoutJsonCreatorExperimental() {
            val mapper: ObjectMapper = ObjectMapper()
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            mapper.configure(SerializationFeature.INDENT_OUTPUT, false)
            mapper.registerModule(JodaModule())
            mapper.registerModule(KotlinModule(false)) // TODO: experimental, ignore JsonCreator requirement because Jackson doesn't check either
            val stateObj = mapper.readValue(normalCasedJson, javaClass<FailWithoutJsonCreator>())!!
            validate(stateObj)
    }

    // testing using custom serializer, JodaDateTime to be sure we don't break working with other modules or complex types

    Test fun testDataClassWithExplicitJsonCreator() {
        // data class with explicit JsonCreator and no parameters with JsonProperty
        val stateObj = normalCasedMapper.readValue(normalCasedJson, javaClass<StateObjectAsDataClassExplicitJsonCreator>())!!
        validate(stateObj)

        val test1out = StringWriter()
        normalCasedMapper.writeValue(test1out, stateObj)

        assertThat(test1out.getBuffer().toString(), equalTo(normalCasedJson))
    }

    Test fun testDataClassWithExplicitJsonCreatorAndJsonProperty() {
        // data class with JsonCreator and JsonProperty
        val stateObj = normalCasedMapper.readValue(normalCasedJson, javaClass<StateObjectAsDataClassWithJsonCreatorAndJsonProperty>())!!
        validate(stateObj)

        val test1out = normalCasedMapper.writeValueAsString(stateObj)
        assertThat(test1out, equalTo(normalCasedJson))
    }

    Test fun testNormalClassWithJsonCreator() {
        // normal class
        val stateObj = normalCasedMapper.readValue(normalCasedJson, javaClass<StateObjectAsNormalClass>())!!
        validate(stateObj)
    }

    Test fun testNormalClassWithPartialConstructorJsonCreator() {
        // normal class with some fields not in constructor
        val stateObj = normalCasedMapper.readValue(normalCasedJson, javaClass<StateObjectWithPartialFieldsInConstructor>())!!
        validate(stateObj)
    }

    Test fun testDataClassWithNonFieldParametersInConstructor() {
        // data class with non fields appearing as parameters in constructor, this works but null values or defaults for primitive types are passed to
        // the unrecognized fields in the constructor.  Does not work with default values for parameters, because a null does not get converted to the
        // default.

        val stateObj = normalCasedMapper.readValue(normalCasedJson, javaClass<StateObjectAsDataClassConfusingConstructor>())!!
        validate(stateObj)
    }

    Test fun findingConstructorsWithPascalCasedJson() {
        // pascal cased strategy for JSON, note that explicit named JsonProperty are not renamed and must be exactly the same
        val stateObj = pascalCasedMapper.readValue(pascalCasedJson, javaClass<StateObjectAsDataClassExplicitJsonCreator>())!!
        validate(stateObj)

        val test1out = pascalCasedMapper.writeValueAsString(stateObj)
        assertThat(test1out, equalTo(pascalCasedJson))
    }

    [Ignore(value = "Factory Methods not supported yet")]
    Test fun findingFactoryMethod() {
        /*
         TODO: this will not work since Kotlin does not have static methods, have to find a way to make factory methods
         work from class objects, maybe a TypeInstantiator would work
         StateObjectWithFactory.object$.create method is the actual factory when compiled
        */
        val stateObj = normalCasedMapper.readValue(normalCasedJson, javaClass<StateObjectWithFactory>())!!
        validate(stateObj)
    }
}

